package dev.tchiwara.ecommerce.api.order;

import dev.tchiwara.ecommerce.api.global.IllegalOrderStateException;
import dev.tchiwara.ecommerce.api.product.Product;
import dev.tchiwara.ecommerce.api.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<OrderItem> items = new ArrayList<>();

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    @Version
    private long version;

    public static Order place(User user) {
        Order order = new Order();
        order.user = Objects.requireNonNull(user);
        order.status = OrderStatus.PENDING;
        order.totalAmount = BigDecimal.ZERO;
        return order;
    }

    public void addItem(Product product, int quantity) {
        items.add(new OrderItem(this, product, quantity));
        recalculateTotal();
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void cancel() {
        requirePending("cancelled");
        status = OrderStatus.CANCELLED;
    }

    public void markPaid() {
        requirePending("paid");
        status = OrderStatus.PAID;
    }

    private void requirePending(String action) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalOrderStateException(
                    "Order %d is %s and cannot be %s"
                            .formatted(id, status, action)
            );
        }
    }

    private void recalculateTotal() {
        totalAmount = items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}