package dev.tchiwara.ecommerce.api.category;
import dev.tchiwara.ecommerce.api.category.dtos.CategoryRequestDTO;
import dev.tchiwara.ecommerce.api.category.dtos.CategoryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel="spring")
public interface CategoryMapper {

    Category toEntity(CategoryRequestDTO categoryRequestDTO);
    CategoryResponseDTO toDTO(Category category);
    void updateCategory(CategoryRequestDTO categoryRequestDTO, @MappingTarget Category category);

}
