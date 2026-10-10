package dev.tchiwara.ecommerce.api.product;

import dev.tchiwara.ecommerce.api.product.dtos.CreateProductRequestDTO;
import dev.tchiwara.ecommerce.api.product.dtos.ProductRequestDTO;
import dev.tchiwara.ecommerce.api.product.dtos.ProductResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel="spring")
public interface ProductMapper {

    @Mapping(target = "category", ignore = true)
    Product toEntity(CreateProductRequestDTO request);

    @Mapping(source = "category.id", target = "categoryId")
    ProductResponseDTO toDTO(Product product);

    @Mapping(target = "stockQuantity", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateProduct(ProductRequestDTO productRequestDTO, @MappingTarget Product product);

}
