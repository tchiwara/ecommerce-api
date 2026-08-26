package dev.tchiwara.ecommerce.api.category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import dev.tchiwara.ecommerce.api.category.dtos.CategoryRequestDTO;
import dev.tchiwara.ecommerce.api.category.dtos.CategoryResponseDTO;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;


    @Test
    void shouldCreateCategory() {

        // Arrange
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setName("Electronics");
        Category category = new Category();
        Category savedCategory = new Category();
        CategoryResponseDTO expectedResponse = new CategoryResponseDTO(1L, "Electronics");

        when(categoryMapper.toEntity(request))
                .thenReturn(category);

        when(categoryRepository.save(category))
                .thenReturn(savedCategory);

        when(categoryMapper.toDTO(savedCategory))
                .thenReturn(expectedResponse);


        // Act
        CategoryResponseDTO actual =
                categoryService.createCategory(request);


        // Assert
        assertEquals(expectedResponse, actual);

        verify(categoryMapper).toEntity(request);
        verify(categoryRepository).save(category);
        verify(categoryMapper).toDTO(savedCategory);
    }

    @Test
    void shouldGetAllCategories() {

        // Arrange
        int page = 0;
        Pageable pageable = PageRequest.of(page, 10, Sort.by("name"));

        Category category = new Category();
        CategoryResponseDTO responseDTO = new CategoryResponseDTO(1L, "Electronics");

        Page<Category> categoryPage = new PageImpl<>(List.of(category), pageable, 1);

        when(categoryRepository.findAll(pageable))
                .thenReturn(categoryPage);

        when(categoryMapper.toDTO(category))
                .thenReturn(responseDTO);

        // Act
        Page<CategoryResponseDTO> actual = categoryService.getAllCategories(page);

        // Assert
        assertEquals(1, actual.getTotalElements());
        assertEquals(responseDTO, actual.getContent().get(0));

        verify(categoryRepository).findAll(pageable);
        verify(categoryMapper).toDTO(category);
    }
    @Test
    void shouldGetCategoryById() {

        // Arrange
        Long id = 1L;
        Category category = new Category();
        CategoryResponseDTO expectedResponse = new CategoryResponseDTO(1L, "Electronics");

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(category));

        when(categoryMapper.toDTO(category))
                .thenReturn(expectedResponse);

        // Act
        CategoryResponseDTO actual = categoryService.getCategoryById(id);

        // Assert
        assertEquals(expectedResponse, actual);

        verify(categoryRepository).findById(id);
        verify(categoryMapper).toDTO(category);
    }

    @Test
    void shouldUpdateCategory() {

        // Arrange
        Long id = 1L;
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setName("Updated Electronics");

        Category category = new Category();
        CategoryResponseDTO expectedResponse = new CategoryResponseDTO(1L, "Updated Electronics");

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(category));

        when(categoryMapper.toDTO(category))
                .thenReturn(expectedResponse);

        // Act
        CategoryResponseDTO actual = categoryService.updateCategory(request, id);

        // Assert
        assertEquals(expectedResponse, actual);

        verify(categoryRepository).findById(id);
        verify(categoryMapper).updateCategory(request, category);
        verify(categoryRepository).save(category);
        verify(categoryMapper).toDTO(category);
    }

    @Test
    void shouldDeleteCategory() {

        // Arrange
        Long id = 1L;
        Category category = new Category();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(category));

        // Act
        categoryService.deleteCategory(id);

        // Assert
        verify(categoryRepository).findById(id);
        verify(categoryRepository).delete(category);
    }
}