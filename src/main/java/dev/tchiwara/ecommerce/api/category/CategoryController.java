package dev.tchiwara.ecommerce.api.category;

import dev.tchiwara.ecommerce.api.category.dtos.CategoryRequestDTO;
import dev.tchiwara.ecommerce.api.category.dtos.CategoryResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories(){
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @PathVariable Byte id
    ){
            return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
          @Valid  @RequestBody CategoryRequestDTO categoryRequestDTO,
            @PathVariable Byte id
    ){
        var response=categoryService.updateCategory(categoryRequestDTO, id);
        return ResponseEntity.ok(response);
    }

}
