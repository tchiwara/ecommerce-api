package dev.tchiwara.ecommerce.api.auth.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResendOtpRequestDTO {
    @NotBlank
    private String registrationId;
}
