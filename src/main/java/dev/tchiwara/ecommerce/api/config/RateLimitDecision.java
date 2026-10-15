package dev.tchiwara.ecommerce.api.config;

public record RateLimitDecision(
        boolean allowed,
        long limit,
        long remaining,
        long retryAfterSeconds
) {
}