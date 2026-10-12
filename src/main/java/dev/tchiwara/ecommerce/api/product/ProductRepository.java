package dev.tchiwara.ecommerce.api.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product,Long> {


    @Modifying
    @Query("""
        UPDATE Product p
           SET p.stockQuantity = p.stockQuantity - :qty
         WHERE p.id = :id AND p.stockQuantity >= :qty
        """)
    int decrementStock(@Param("id") Long id, @Param("qty") int qty);

    @Modifying
    @Query("""
        UPDATE Product p
           SET p.stockQuantity = p.stockQuantity + :qty
         WHERE p.id = :id
        """)
    int incrementStock(@Param("id") Long id, @Param("qty") int qty);

}
