package dev.tchiwara.ecommerce.api.order;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByIdAndUser_Id(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT DISTINCT o
            FROM Order o
            LEFT JOIN FETCH o.items
            LEFT JOIN FETCH o.user
            WHERE o.id = :id
            """)
    Optional<Order> findByIdWithItemsAndUser(@Param("id") Long id);

    @EntityGraph(attributePaths = {"items", "user"})
    Optional<Order> findByStripeSessionId(String stripeSessionId);

    Optional<Order> findByStripePaymentIntentId(
            String stripePaymentIntentId
    );
}