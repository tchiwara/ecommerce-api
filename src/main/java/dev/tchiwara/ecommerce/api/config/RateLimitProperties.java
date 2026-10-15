package dev.tchiwara.ecommerce.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
@Getter
@Setter
public class RateLimitProperties {

    private boolean enabled = true;
    private String keyPrefix = "ecom:rl:";
    private long windowSeconds = 60;
    private boolean trustedProxy;

    private Strict strict = new Strict();
    private Moderate moderate = new Moderate();
    private Loose loose = new Loose();

    @Getter
    @Setter
    public static class Strict {

        private long loginLimit = 10;
        private long registrationLimit = 5;
        private long otpSendLimit = 5;
        private long otpVerifyLimit = 10;
    }

    @Getter
    @Setter
    public static class Moderate {

        private long authenticatedLimit = 120;
    }

    @Getter
    @Setter
    public static class Loose {

        private long publicGetLimit = 300;
    }
}