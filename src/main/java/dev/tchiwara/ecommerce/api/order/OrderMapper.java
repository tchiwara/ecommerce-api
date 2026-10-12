package dev.tchiwara.ecommerce.api.order;

import dev.tchiwara.ecommerce.api.order.dtos.OrderItemResponseDTO;
import dev.tchiwara.ecommerce.api.order.dtos.OrderResponseDTO;
import dev.tchiwara.ecommerce.api.order.dtos.OrderSummaryResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponseDTO toResponse(Order order);

    OrderSummaryResponseDTO toSummary(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "subtotal", expression = "java(item.subtotal())")
    OrderItemResponseDTO toItemResponse(OrderItem item);
}