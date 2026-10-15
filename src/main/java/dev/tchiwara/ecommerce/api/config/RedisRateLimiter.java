package dev.tchiwara.ecommerce.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisRateLimiter {

    private final StringRedisTemplate redis;
    private final RedisScript<List> rateLimitScript;
    private final RateLimitProperties properties;

    public RateLimitDecision check(
            String bucket,
            String identifier,
            long limit,
            Duration window
    ) {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException(
                    "Rate-limit bucket cannot be blank"
            );
        }

        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException(
                    "Rate-limit identifier cannot be blank"
            );
        }

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "Rate-limit limit must be greater than zero"
            );
        }

        if (window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException(
                    "Rate-limit window must be greater than zero"
            );
        }

        String key = buildKey(bucket, identifier);
        long windowSeconds = Math.max(1, window.getSeconds());

        List<?> result = redis.execute(
                rateLimitScript,
                List.of(key),
                String.valueOf(windowSeconds),
                String.valueOf(limit)
        );

        if (result == null || result.size() < 3) {
            throw new IllegalStateException(
                    "Redis rate-limit script returned an invalid result"
            );
        }

        long allowedValue = numberAt(result, 0);
        long remaining = Math.max(0, numberAt(result, 1));
        long retryAfterSeconds = Math.max(1, numberAt(result, 2));

        return new RateLimitDecision(
                allowedValue == 1,
                limit,
                remaining,
                retryAfterSeconds
        );
    }

    private long numberAt(
            List<?> result,
            int index
    ) {
        Object value = result.get(index);

        if (!(value instanceof Number number)) {
            throw new IllegalStateException(
                    "Redis rate-limit script returned a non-numeric value"
            );
        }

        return number.longValue();
    }

    private String buildKey(
            String bucket,
            String identifier
    ) {
        return properties.getKeyPrefix()
                + bucket
                + ":"
                + identifier;
    }
}