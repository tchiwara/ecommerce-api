package dev.tchiwara.ecommerce.api.auth.otp;

import dev.tchiwara.ecommerce.api.global.EmailAlreadyRegisteredException;
import dev.tchiwara.ecommerce.api.user.Role;
import dev.tchiwara.ecommerce.api.user.User;
import dev.tchiwara.ecommerce.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redis;
    private final OtpHasher otpHasher;
    private final OtpConfig otpConfig;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final SecureRandom secureRandom = new SecureRandom();

    private String normalize(String email) { return email.trim().toLowerCase(); }
    private String dataKey(String regId)      { return "reg:" + regId; }
    private String attemptsKey(String regId)  { return "reg:attempts:" + regId; }
    private String cooldownKey(String email)  { return "reg:cooldown:" + email; }
    private String hourlyKey(String email)    { return "reg:hourly:" + email; }

    private HashOperations<String, String, String> hashOps() {
        return redis.opsForHash();
    }

    public String startRegistration(String name, String rawEmail, String rawPassword) {
        String email = normalize(rawEmail);

        if (userRepository.findByEmail(email).map(User::isEmailVerified).orElse(false)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        String registrationId = UUID.randomUUID().toString();
        String passwordHash = passwordEncoder.encode(rawPassword);

        hashOps().put(dataKey(registrationId), "email", email);
        hashOps().put(dataKey(registrationId), "name", name);
        hashOps().put(dataKey(registrationId), "passwordHash", passwordHash);
        redis.expire(dataKey(registrationId), Duration.ofMinutes(otpConfig.getTtlMinutes()));

        issueCodeIfAllowed(registrationId, email, name);
        return registrationId;
    }

    public void resendOtp(String registrationId) {
        String email = hashOps().get(dataKey(registrationId), "email");
        if (email == null) return; // unknown or expired attempt
        String name = hashOps().get(dataKey(registrationId), "name");
        issueCodeIfAllowed(registrationId, email, name);
    }

    private void issueCodeIfAllowed(String registrationId, String email, String name) {
        Boolean allowedToSend = redis.opsForValue().setIfAbsent(
                cooldownKey(email), "1", Duration.ofSeconds(otpConfig.getResendCooldownSeconds()));
        if (!Boolean.TRUE.equals(allowedToSend)) return;

        Long sends = redis.opsForValue().increment(hourlyKey(email));
        if (sends != null && sends == 1) redis.expire(hourlyKey(email), Duration.ofHours(1));
        if (sends != null && sends > otpConfig.getHourlyLimit()) return;

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        hashOps().put(dataKey(registrationId), "codeHash", otpHasher.hash(registrationId, code));
        redis.expire(dataKey(registrationId), Duration.ofMinutes(otpConfig.getTtlMinutes()));
        redis.delete(attemptsKey(registrationId));

        eventPublisher.publishEvent(new OtpIssuedEvent(email, name, code));
    }

    @Transactional
    public boolean verifyRegistration(String registrationId, String code) {
        Map<String, String> data = hashOps().entries(dataKey(registrationId));
        String storedHash = data.get("codeHash");
        if (storedHash == null) return false; // no record, or no code issued to it yet

        Long attempts = redis.opsForValue().increment(attemptsKey(registrationId));
        if (attempts != null && attempts == 1) {
            redis.expire(attemptsKey(registrationId), Duration.ofMinutes(otpConfig.getTtlMinutes()));
        }
        if (attempts == null || attempts > otpConfig.getMaxAttempts()) return false;

        if (!otpHasher.matches(registrationId, code, storedHash)) return false;

        String email = data.get("email");
        if (userRepository.findByEmail(email).map(User::isEmailVerified).orElse(false)) return false;

        var user = new User();
        user.setName(data.get("name"));
        user.setEmail(email);
        user.setPasswordHash(data.get("passwordHash"));
        user.setRole(Role.CUSTOMER);
        user.setEmailVerified(true);

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            return false; // very rare race; this attempt's data is untouched, retry possible
        }

        redis.delete(List.of(dataKey(registrationId), attemptsKey(registrationId)));
        return true;
    }
}