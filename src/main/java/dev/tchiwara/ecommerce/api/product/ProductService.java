package dev.tchiwara.ecommerce.api.product;

import dev.tchiwara.ecommerce.api.category.Category;
import dev.tchiwara.ecommerce.api.category.CategoryRepository;
import dev.tchiwara.ecommerce.api.global.InsufficientStockException;
import dev.tchiwara.ecommerce.api.global.ResourceNotFoundException;
import dev.tchiwara.ecommerce.api.product.dtos.CreateProductRequestDTO;
import dev.tchiwara.ecommerce.api.product.dtos.ProductRequestDTO;
import dev.tchiwara.ecommerce.api.product.dtos.ProductResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

private final  ProductRepository productRepository;
private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    public ProductResponseDTO createProduct(CreateProductRequestDTO createProductRequestDTO) {

    var category=categoryRepository.findById(createProductRequestDTO.getCategoryId())
            .orElseThrow(
                    ()-> new ResourceNotFoundException("Category of id "+createProductRequestDTO.getCategoryId()+" not found!")
            );
    Product product=productMapper.toEntity(createProductRequestDTO);
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

    public ProductResponseDTO updateProduct(
            ProductRequestDTO productRequestDTO,
            Long id
    ){
        Category category = categoryRepository.findById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category with id " + productRequestDTO.getCategoryId() + " not found")
                );

        var product=productRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product with id " + id + " not found")
                );

        product.setCategory(category);

        productMapper.updateProduct(productRequestDTO, product);
        productRepository.save(product);
        return productMapper.toDTO(product);
    }

    public void deleteProduct(Long id){
        var product=productRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Product with id " + id + " not found")
                );
        productRepository.delete(product);
    }


    @Transactional
    public ProductResponseDTO adjustStock(Long id, int delta) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product with id " + id + " not found");
        }
        if (delta > 0) {
            productRepository.incrementStock(id, delta);
        } else if (delta < 0 && productRepository.decrementStock(id, -delta) == 0) {
            throw new InsufficientStockException("product " + id);
        }
        var product = productRepository.findById(id).orElseThrow();
        return productMapper.toDTO(product);
    }


}
