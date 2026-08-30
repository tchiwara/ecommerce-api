package dev.tchiwara.ecommerce.api.product;

import dev.tchiwara.ecommerce.api.category.CategoryRepository;
import dev.tchiwara.ecommerce.api.global.ResourceNotFoundException;
import dev.tchiwara.ecommerce.api.product.dtos.ProductRequestDTO;
import dev.tchiwara.ecommerce.api.product.dtos.ProductResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<ProductResponseDTO> getAllProducts(
            int page
    ){
        Pageable pageable= PageRequest.of(page,10, Sort.by("name"));
        Page<Product> products=productRepository.findAll(pageable);

        return products
                .map(productMapper::toDTO);
    }

    public ProductResponseDTO getProductById(Long id){
        var product=productRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product with id " + id + " not found")
                );
        return productMapper.toDTO(product);
    }

}
