package dev.tchiwara.ecommerce.api.category;

import dev.tchiwara.ecommerce.api.category.dtos.CategoryRequestDTO;
import dev.tchiwara.ecommerce.api.category.dtos.CategoryResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final  CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> addCategory(
           @Valid @RequestBody CategoryRequestDTO categoryRequestDTO,
            UriComponentsBuilder uriBuilder
            ){
        CategoryResponseDTO response=categoryService.createCategory(categoryRequestDTO);

        var uri=uriBuilder
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedModel<CategoryResponseDTO>> getAllCategories(
            @RequestParam(defaultValue = "0") int page
    ){
        Page<CategoryResponseDTO> categoryPage=categoryService.getAllCategories(page);
        return ResponseEntity.ok(new PagedModel<>(categoryPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @PathVariable Long id
    ){
            return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
          @Valid  @RequestBody CategoryRequestDTO categoryRequestDTO,
            @PathVariable Long id
    ){
        var response=categoryService.updateCategory(categoryRequestDTO, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long id
    ){
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
