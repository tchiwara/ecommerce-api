package dev.tchiwara.ecommerce.api.product;

import dev.tchiwara.ecommerce.api.category.Category;
import dev.tchiwara.ecommerce.api.category.dtos.CategoryRequestDTO;
import dev.tchiwara.ecommerce.api.product.dtos.ProductRequestDTO;
import dev.tchiwara.ecommerce.api.product.dtos.ProductResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel="spring")
public interface ProductMapper {

    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductRequestDTO productRequestDTO);

    @Mapping(source = "category.id", target = "categoryId")
    ProductResponseDTO toDTO(Product product);

    @Mapping(target = "category", ignore = true)
    void updateProduct(ProductRequestDTO productRequestDTO, @MappingTarget Product product);

}
