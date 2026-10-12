package dev.tchiwara.ecommerce.api.order;

import dev.tchiwara.ecommerce.api.global.ResourceNotFoundException;
import dev.tchiwara.ecommerce.api.order.dtos.OrderResponseDTO;
import dev.tchiwara.ecommerce.api.order.dtos.OrderSummaryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponseDTO> listOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUser_Id(userId, pageable).map(orderMapper::toSummary);
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrder(Long userId, Long orderId) {
        return orderRepository.findByIdAndUser_Id(orderId, userId)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
}
