package dev.tchiwara.ecommerce.api.security;

import dev.tchiwara.ecommerce.api.config.RateLimitDecision;
import dev.tchiwara.ecommerce.api.config.RateLimitProperties;
import dev.tchiwara.ecommerce.api.config.RedisRateLimiter;
import dev.tchiwara.ecommerce.api.auth.filters.JwtAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String WEBHOOK_PATH = "/payments/webhook";
    private static final String LOGIN_PATH = "/auth/login";
    private static final String REGISTER_PATH = "/users";
    private static final String RESEND_OTP_PATH = "/auth/resend-otp";
    private static final String VERIFY_OTP_PATH = "/auth/verify-otp";

    private final RedisRateLimiter rateLimiter;
    private final RateLimitProperties properties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (!properties.isEnabled()
                || isOptionsRequest(request)
                || isStripeWebhook(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitRule rule = resolveRule(request);

        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String identifier = resolveIdentifier(request, rule);

        if (identifier == null) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitDecision decision;

        try {
            decision = rateLimiter.check(
                    rule.bucket(),
                    identifier,
                    rule.limit(),
                    Duration.ofSeconds(properties.getWindowSeconds())
            );
        } catch (Exception exception) {
            /*
             * Fail open intentionally. Redis is an availability dependency
             * for rate limiting, not for serving the API.
             */
            log.warn(
                    "Redis rate limiter unavailable; allowing request to proceed"
            );

            filterChain.doFilter(request, response);
            return;
        }

        addRateLimitHeaders(response, decision);

        if (!decision.allowed()) {
            writeTooManyRequests(response, decision);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private RateLimitRule resolveRule(
            HttpServletRequest request
    ) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if (HttpMethod.POST.matches(method)
                && LOGIN_PATH.equals(path)) {
            return new RateLimitRule(
                    "login",
                    properties.getStrict().getLoginLimit(),
                    RateLimitKeyType.IP
            );
        }

        if (HttpMethod.POST.matches(method)
                && REGISTER_PATH.equals(path)) {
            return new RateLimitRule(
                    "registration",
                    properties.getStrict().getRegistrationLimit(),
                    RateLimitKeyType.IP
            );
        }

        if (HttpMethod.POST.matches(method)
                && RESEND_OTP_PATH.equals(path)) {
            return new RateLimitRule(
                    "otp-send",
                    properties.getStrict().getOtpSendLimit(),
                    RateLimitKeyType.IP
            );
        }

        if (HttpMethod.POST.matches(method)
                && VERIFY_OTP_PATH.equals(path)) {
            return new RateLimitRule(
                    "otp-verify",
                    properties.getStrict().getOtpVerifyLimit(),
                    RateLimitKeyType.IP
            );
        }

        if (HttpMethod.GET.matches(method)
                && isPublicGetPath(path)) {
            return new RateLimitRule(
                    "public-get",
                    properties.getLoose().getPublicGetLimit(),
                    RateLimitKeyType.IP
            );
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && isJwtUserPrincipal(authentication)) {
            return new RateLimitRule(
                    "authenticated",
                    properties.getModerate().getAuthenticatedLimit(),
                    RateLimitKeyType.USER_ID
            );
        }

        return null;
    }

    private String resolveIdentifier(
            HttpServletRequest request,
            RateLimitRule rule
    ) {
        if (rule.keyType() == RateLimitKeyType.IP) {
            return resolveClientIp(request);
        }

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !isJwtUserPrincipal(authentication)) {
            return null;
        }

        /*
         * JwtAuthenticationFilter stores the numeric user ID as the
         * authentication principal. No email, JWT, or request body is used.
         */
        return authentication.getName();
    }

    private String resolveClientIp(
            HttpServletRequest request
    ) {
        if (properties.isTrustedProxy()) {
            String forwardedFor = request.getHeader("X-Forwarded-For");

            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor
                        .split(",", 2)[0]
                        .trim();
            }
        }

        return request.getRemoteAddr();
    }

    private boolean isJwtUserPrincipal(
            Authentication authentication
    ) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof Number) {
            return true;
        }

        if (principal instanceof String value) {
            return value.matches("\\d+");
        }

        return false;
    }

    private boolean isPublicGetPath(
            String path
    ) {
        return path.equals("/products")
                || path.startsWith("/products/")
                || path.equals("/categories")
                || path.startsWith("/categories/");
    }

    private boolean isStripeWebhook(
            HttpServletRequest request
    ) {
        return HttpMethod.POST.matches(request.getMethod())
                && WEBHOOK_PATH.equals(request.getRequestURI());
    }

    private boolean isOptionsRequest(
            HttpServletRequest request
    ) {
        return HttpMethod.OPTIONS.matches(request.getMethod());
    }

    private void addRateLimitHeaders(
            HttpServletResponse response,
            RateLimitDecision decision
    ) {
        response.setHeader(
                "X-RateLimit-Limit",
                String.valueOf(decision.limit())
        );

        response.setHeader(
                "X-RateLimit-Remaining",
                String.valueOf(decision.remaining())
        );
    }

    private void writeTooManyRequests(
            HttpServletResponse response,
            RateLimitDecision decision
    ) throws IOException {
        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        response.setHeader(
                "Retry-After",
                String.valueOf(decision.retryAfterSeconds())
        );

        response.getWriter().write(
                "{\"message\":\"Too many requests. Please try again later.\"}"
        );
    }

    private enum RateLimitKeyType {
        IP,
        USER_ID
    }

    private record RateLimitRule(
            String bucket,
            long limit,
            RateLimitKeyType keyType
    ) {
    }
}