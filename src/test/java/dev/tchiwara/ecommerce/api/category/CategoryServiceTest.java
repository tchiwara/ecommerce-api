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
}