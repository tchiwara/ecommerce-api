package dev.tchiwara.ecommerce.api.auth;

import dev.tchiwara.ecommerce.api.auth.dtos.JwtResponse;
import dev.tchiwara.ecommerce.api.auth.dtos.LoginRequest;
import dev.tchiwara.ecommerce.api.auth.dtos.ResendOtpRequestDTO;
import dev.tchiwara.ecommerce.api.auth.dtos.VerifyOtpRequestDTO;
import dev.tchiwara.ecommerce.api.auth.otp.EmailVerificationService;
import dev.tchiwara.ecommerce.api.config.JwtConfig;
import dev.tchiwara.ecommerce.api.user.UserMapper;
import dev.tchiwara.ecommerce.api.user.UserRepository;
import dev.tchiwara.ecommerce.api.user.dtos.UserResponseDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtConfig jwtConfig;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> loginRequest(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
    ){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        var user= userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();

        if (!user.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        var cookie=new Cookie("refreshToken",refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponse(accessToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(
            @CookieValue(value="refreshToken") String refreshToken
    ){
        if(!jwtService.validateToken(refreshToken)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var userId= jwtService.getUserIdFromToken(refreshToken);
        var user=userRepository.findById(userId).orElseThrow();
        var accessToken = jwtService.generateAccessToken(user);

        return ResponseEntity.ok(new JwtResponse(accessToken));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(@AuthenticationPrincipal Long userId){

        var user=userRepository.findById(userId).orElse(null);
        if(user==null) return ResponseEntity.notFound().build();
        var userDto=userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<Map<String, String>> resendOtp(@Valid @RequestBody ResendOtpRequestDTO request) {
        emailVerificationService.resendOtp(request.getRegistrationId());
        return ResponseEntity.accepted().body(Map.of("message", "If eligible, a new code has been sent."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequestDTO request) {
        boolean verified = emailVerificationService.verifyRegistration(
                request.getRegistrationId(), request.getCode());
        return verified
                ? ResponseEntity.ok(Map.of("message", "Email verified. Account created."))
                : ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired code"));
    }

}


