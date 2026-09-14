package dev.tchiwara.ecommerce.api.cart;

import dev.tchiwara.ecommerce.api.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="cart_items")
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
   private int quantity;

   @Column(name = "unit_price")
   private BigDecimal unitPrice;

    @Column(name = "created_at",updatable = false,insertable = false)
    @Generated(event = EventType.INSERT)
    private Instant createdAt;

    @Column(name = "updated_at",updatable = false,insertable = false)
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    private Instant updatedAt;

    public CartItem(Cart cart, Product product,int quantity) {

        if (cart == null) {
            throw new IllegalArgumentException("Cart cannot be null");
        }

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        validateQuantity(quantity);

        this.cart=cart;
        this.product=product;
        this.unitPrice=product.getPrice();
        this.quantity=quantity;
    }

    public void changeQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    private static void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than 0"
            );
        }
    }

    public void increaseQuantity() {
       this.quantity++;
    }

    public BigDecimal getSubtotal() {
        return unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
    }

}
