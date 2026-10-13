package dev.tchiwara.ecommerce.api.order;

import dev.tchiwara.ecommerce.api.global.IllegalOrderStateException;
import dev.tchiwara.ecommerce.api.product.Product;
import dev.tchiwara.ecommerce.api.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
            cascade = jakarta.persistence.CascadeType.ALL,
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

    @Column(name = "stripe_session_id", unique = true, length = 255)
    private String stripeSessionId;

    @Column(name = "stripe_payment_intent_id", length = 255)
    private String stripePaymentIntentId;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "confirmation_email_sent_at")
    private Instant confirmationEmailSentAt;

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

    public void attachStripeSession(String stripeSessionId) {
        if (stripeSessionId == null || stripeSessionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Stripe session ID cannot be blank"
            );
        }

        this.stripeSessionId = stripeSessionId;
    }

    public void markPaid(
            String stripeSessionId,
            String stripePaymentIntentId
    ) {
        requirePending("paid");

        this.status = OrderStatus.PAID;
        this.stripeSessionId = stripeSessionId;
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.paidAt = Instant.now();
    }

    public void cancel() {
        requirePending("cancelled");
        status = OrderStatus.CANCELLED;
    }

    public void expire() {
        requirePending("expired");
        status = OrderStatus.EXPIRED;
    }

    public void markConfirmationEmailSent() {
        if (confirmationEmailSentAt == null) {
            confirmationEmailSentAt = Instant.now();
        }
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