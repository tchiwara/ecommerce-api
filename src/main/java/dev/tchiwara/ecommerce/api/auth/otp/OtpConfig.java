package dev.tchiwara.ecommerce.api.auth.otp;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.otp")
@Getter
@Setter
public class OtpConfig {
    private String secret;
    private int ttlMinutes = 10;
    private int maxAttempts = 5;
    private int resendCooldownSeconds = 60;
    private int hourlyLimit = 5;
}
