package dev.tchiwara.ecommerce.api.product;

import dev.tchiwara.ecommerce.api.category.CategoryRepository;
import dev.tchiwara.ecommerce.api.global.ResourceNotFoundException;
import dev.tchiwara.ecommerce.api.product.dtos.ProductRequestDTO;
import dev.tchiwara.ecommerce.api.product.dtos.ProductResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

private final  ProductRepository productRepository;
private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {

    var category=categoryRepository.findById(productRequestDTO.getCategoryId())
            .orElseThrow(
                    ()-> new ResourceNotFoundException("Category of id "+productRequestDTO.getCategoryId()+" not found!")
            );
    Product product=productMapper.toEntity(productRequestDTO);
    product.setCategory(category);
    var savedProduct=productRepository.save(product);

    return productMapper.toDTO(savedProduct);


}
}
