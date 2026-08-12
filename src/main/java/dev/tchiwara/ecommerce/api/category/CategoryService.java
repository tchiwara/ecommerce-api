package dev.tchiwara.ecommerce.api.category;

import dev.tchiwara.ecommerce.api.category.dtos.CategoryRequestDTO;
import dev.tchiwara.ecommerce.api.category.dtos.CategoryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;

    public CategoryResponseDTO createCategory(CategoryRequestDTO categoryRequestDTO) {

        Category category=categoryMapper.toEntity(categoryRequestDTO);

        Category savedCategory=categoryRepository.save(category);

        return categoryMapper.toDTO(savedCategory);

    }

    public List<CategoryResponseDTO> getAllCategories(){
        List<Category> categories=categoryRepository.findAll();

        return categories
                .stream()
                .map(categoryMapper::toDTO)
                .toList();
    }
}