package dev.tchiwara.ecommerce.api.cart;

import dev.tchiwara.ecommerce.api.product.Product;
import dev.tchiwara.ecommerce.api.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="carts")
public class Cart {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "id",updatable = false,nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at",updatable = false,insertable = false)
    @Generated(event = EventType.INSERT)
    private Instant createdAt;

    @Column(name = "updated_at",updatable = false,insertable = false)
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    private Instant updatedAt;

    @OneToMany(mappedBy = "cart",fetch =FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    private Set<CartItem> cartItems=new HashSet<>();

    public CartItem getCartItem(Long productId) {

        return cartItems
                .stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    public CartItem addCartItem(Product product) {

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }


        var cartItem=getCartItem(product.getId());

        if(cartItem!=null) {
            cartItem.increaseQuantity();
        }

        else{
            cartItem=new CartItem(
                    this,
                    product,
                    1);

            cartItems.add(cartItem);
        }
        return cartItem;
    }

    public BigDecimal getTotal() {

        return cartItems
                .stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

    }

    public static Cart newCartFor(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        Cart cart = new Cart();
        cart.user = user;

        return cart;
    }

    public CartItem getCartItemOrThrow(Long productId) {
        CartItem item = getCartItem(productId);
        if (item == null) {
            throw new ItemNotInCartException(productId);
        }
        return item;
    }

    public void removeCartItem(Long productId) {
        CartItem item = getCartItemOrThrow(productId);
        cartItems.remove(item);
    }

}


/*
* @NoArgsConstructor(access = AccessLevel.PROTECTED)
* "The above means Hibernate is allowed to construct my entity for persistence purposes,
* but my business code must construct it through the rules of my domain."
*
* @Generated -> tells Hibernate to read the DB-generated value back into this Java object
* */
