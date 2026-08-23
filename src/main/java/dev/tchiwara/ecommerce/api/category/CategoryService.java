package dev.tchiwara.ecommerce.api.category;

import dev.tchiwara.ecommerce.api.category.dtos.CategoryRequestDTO;
import dev.tchiwara.ecommerce.api.category.dtos.CategoryResponseDTO;
import dev.tchiwara.ecommerce.api.global.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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

    public Page<CategoryResponseDTO> getAllCategories(
            int page
    ){
        Pageable pageable= PageRequest.of(page,10, Sort.by("name"));
        Page<Category> categories=categoryRepository.findAll(pageable);

        return categories
                .map(categoryMapper::toDTO);
    }

    public CategoryResponseDTO getCategoryById(Long id){
        var category=categoryRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category with id " + id + " not found")
                );
        return categoryMapper.toDTO(category);
    }

    public CategoryResponseDTO updateCategory(
            CategoryRequestDTO categoryRequestDTO,
            Long id
    ){
        var category=categoryRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category with id " + id + " not found")
                );

        categoryMapper.updateCategory(categoryRequestDTO, category);
        categoryRepository.save(category);
        return categoryMapper.toDTO(category);
    }

    public void deleteCategory(Long id){
        var category=categoryRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category with id " + id + " not found")
                );
        categoryRepository.delete(category);
    }
}