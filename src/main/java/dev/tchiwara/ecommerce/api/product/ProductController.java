package dev.tchiwara.ecommerce.api.product;

import dev.tchiwara.ecommerce.api.product.dtos.ProductRequestDTO;
import dev.tchiwara.ecommerce.api.product.dtos.ProductResponseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponseDTO> addProduct(
          @Valid @RequestBody ProductRequestDTO productRequestDTO,
          UriComponentsBuilder uriBuilder
    ){
        ProductResponseDTO response=productService.createProduct(productRequestDTO);

        var uri=uriBuilder
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedModel<ProductResponseDTO>> getAllProducts(
            @RequestParam(defaultValue = "0")  @Min(0) int page
    ){
        Page<ProductResponseDTO> productPage=productService.getAllProducts(page);
        return ResponseEntity.ok(new PagedModel<>(productPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @Valid  @RequestBody ProductRequestDTO productRequestDTO,
            @PathVariable Long id
    ){
        var response=productService.updateProduct(productRequestDTO, id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id
    ){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

}
