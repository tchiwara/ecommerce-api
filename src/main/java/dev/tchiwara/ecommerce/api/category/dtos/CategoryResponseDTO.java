package dev.tchiwara.ecommerce.api.category.dtos;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CategoryResponseDTO {
    private final Byte id;
    private final String name;
}
