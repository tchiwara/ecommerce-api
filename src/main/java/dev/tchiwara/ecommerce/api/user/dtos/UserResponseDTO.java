package dev.tchiwara.ecommerce.api.user.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserResponseDTO {

    private final Long id;
    private final String name;
    private final String email;
    private final LocalDateTime createdAt;

}
