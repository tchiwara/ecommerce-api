package dev.tchiwara.ecommerce.api.order;

import dev.tchiwara.ecommerce.api.global.ResourceNotFoundException;
import dev.tchiwara.ecommerce.api.order.dtos.OrderResponseDTO;
import dev.tchiwara.ecommerce.api.order.dtos.OrderSummaryResponseDTO;
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
        Order order = orderRepository
                .findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        /*
         * This validates that the order is still PENDING.
         * If another request changes the order first, @Version causes
         * the transaction to fail instead of restoring stock twice.
         */
        order.cancel();

        /*
         * Use a consistent product ordering. This reduces the possibility
         * of deadlocks when multiple orders are cancelled concurrently.
         */
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

        return orderMapper.toResponse(order);
    }
}