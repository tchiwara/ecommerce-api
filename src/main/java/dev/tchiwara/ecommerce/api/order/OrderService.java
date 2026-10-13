package dev.tchiwara.ecommerce.api.order;

import dev.tchiwara.ecommerce.api.global.IllegalOrderStateException;
import dev.tchiwara.ecommerce.api.global.ResourceNotFoundException;
import dev.tchiwara.ecommerce.api.order.dtos.OrderResponseDTO;
import dev.tchiwara.ecommerce.api.order.dtos.OrderSummaryResponseDTO;
import dev.tchiwara.ecommerce.api.payment.PaymentGateway;
import dev.tchiwara.ecommerce.api.payment.dtos.CheckoutSessionResponseDTO;
import dev.tchiwara.ecommerce.api.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductRepository productRepository;
    private final PaymentGateway paymentGateway;

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponseDTO> listOrders(
            Long userId,
            Pageable pageable
    ) {
        return orderRepository
                .findByUser_Id(userId, pageable)
                .map(orderMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(
            Long userId,
            Long orderId
    ) {
        return orderRepository
                .findByIdAndUser_Id(orderId, userId)
                .map(orderMapper::toResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));
    }

    @Transactional
    public OrderResponseDTO cancelOrder(
            Long userId,
            Long orderId
    ) {
        Order order = findOwnedOrder(orderId, userId);

        /*
         * Closing the existing Checkout Session prevents the customer
         * from paying a session for an order that is being cancelled.
         *
         * Stripe already-expired or already-completed sessions are handled
         * gracefully by StripePaymentGateway.
         */
        paymentGateway.expireCheckoutSession(
                order.getStripeSessionId()
        );

        order.cancel();
        restoreStock(order);

        return orderMapper.toResponse(order);
    }

    @Transactional
    public CheckoutSessionResponseDTO createPaymentSession(
            Long userId,
            Long orderId
    ) {
        Order order = findOwnedOrder(orderId, userId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalOrderStateException(
                    "Order %d is %s and cannot create a payment session"
                            .formatted(order.getId(), order.getStatus())
            );
        }

        /*
         * Expire the previous session before creating a replacement.
         * Stripe already-expired or completed sessions are treated as
         * closed by the gateway.
         */
        paymentGateway.expireCheckoutSession(
                order.getStripeSessionId()
        );

        PaymentGateway.CheckoutSession checkoutSession =
                paymentGateway.createCheckoutSession(order);

        order.attachStripeSession(checkoutSession.getSessionId());
        orderRepository.save(order);

        return new CheckoutSessionResponseDTO(
                order.getId(),
                checkoutSession.getCheckoutUrl()
        );
    }

    @Transactional
    public void expireOrderAndRestoreStock(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        order.expire();
        restoreStock(order);
    }

    public Order findOrderByStripeSessionId(String stripeSessionId) {
        return orderRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));
    }

    public Order findOrderByStripePaymentIntentId(
            String stripePaymentIntentId
    ) {
        return orderRepository
                .findByStripePaymentIntentId(stripePaymentIntentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));
    }

    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));
    }

    private Order findOwnedOrder(Long orderId, Long userId) {
        return orderRepository
                .findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));
    }

    private void restoreStock(Order order) {
        order.getItems()
                .stream()
                .sorted(Comparator.comparing(item ->
                        item.getProduct().getId()))
                .forEach(item ->
                        productRepository.incrementStock(
                                item.getProduct().getId(),
                                item.getQuantity()
                        )
                );
    }
}