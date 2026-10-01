package dev.tchiwara.ecommerce.api.auth.otp;

public record OtpIssuedEvent(String email, String name, String code) {}
