package dev.tchiwara.ecommerce.api.user.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequestDTO {

    @NotBlank(message = "User name is required!")
    private String name;

}
