package dev.tchiwara.ecommerce.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@Configuration
public class RateLimitRedisConfig {

    @Bean
    public RedisScript<List> rateLimitScript() {
        DefaultRedisScript<List> script =
                new DefaultRedisScript<>();

        script.setLocation(
                new ClassPathResource("scripts/rate_limit.lua")
        );

        script.setResultType(List.class);

        return script;
    }
}