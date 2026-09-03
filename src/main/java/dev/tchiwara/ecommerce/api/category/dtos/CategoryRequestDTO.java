package dev.tchiwara.ecommerce.api.category.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryRequestDTO {
    @NotBlank(message = "Category name is required!!")
    private String name;
}
