package dev.tchiwara.ecommerce.api.cart;

import dev.tchiwara.ecommerce.api.product.Product;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

   @ManyToOne
   @JoinColumn(name = "cart_id")
   private Cart cart;

   @ManyToOne
   @JoinColumn(name = "product_id")
   private Product product;

   @Column(name ="quantity")
   private Integer quantity;

   @Column(name = "unit_price")
   private BigDecimal unitPrice;

   @Column(name = "created_at")
   private Instant createdAt;

   @Column(name = "updated_at")
   private Instant updatedAt;
}
