package dev.tchiwara.ecommerce.api.cart;


import dev.tchiwara.ecommerce.api.cart.dtos.CartItemResponseDTO;
import dev.tchiwara.ecommerce.api.cart.dtos.CartResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "subtotal", expression = "java(item.getSubtotal())")
    CartItemResponseDTO toItemResponse(CartItem item);

    List<CartItemResponseDTO> toItemResponseList(List<CartItem> items);

    default CartResponseDTO toResponse(Cart cart) {
        CartResponseDTO response = new CartResponseDTO();
        response.setCartId(cart.getId());
        response.setItems(toItemResponseList(cart.getCartItems().stream().toList()));
        response.setTotal(cart.getTotal());
        return response;
    }
}
