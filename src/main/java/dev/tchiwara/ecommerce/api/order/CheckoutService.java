package dev.tchiwara.ecommerce.api.order;

import dev.tchiwara.ecommerce.api.cart.Cart;
import dev.tchiwara.ecommerce.api.cart.CartItem;
import dev.tchiwara.ecommerce.api.cart.CartRepository;
import dev.tchiwara.ecommerce.api.global.EmptyCartException;
import dev.tchiwara.ecommerce.api.global.InsufficientStockException;
import dev.tchiwara.ecommerce.api.global.PriceChangedException;
import dev.tchiwara.ecommerce.api.payment.PaymentGateway;
import dev.tchiwara.ecommerce.api.payment.dtos.CheckoutSessionResponseDTO;
import dev.tchiwara.ecommerce.api.product.Product;
import dev.tchiwara.ecommerce.api.product.ProductRepository;
import dev.tchiwara.ecommerce.api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentGateway paymentGateway;

    @Transactional
    public CheckoutSessionResponseDTO checkout(Long userId) {
        Cart cart = cartRepository.findForCheckout(userId)
                .orElseThrow(EmptyCartException::new);

        if (cart.getCartItems().isEmpty()) {
            throw new EmptyCartException();
        }

        Order order = Order.place(
                userRepository.getReferenceById(userId)
        );

        List<CartItem> lines = cart.getCartItems()
                .stream()
                .sorted(Comparator.comparing(item ->
                        item.getProduct().getId()))
                .toList();

        for (CartItem line : lines) {
            Product product = line.getProduct();

            if (product.getPrice().compareTo(line.getUnitPrice()) != 0) {
                throw new PriceChangedException(
                        product.getName(),
                        line.getUnitPrice(),
                        product.getPrice()
                );
            }

            int updatedRows = productRepository.decrementStock(
                    product.getId(),
                    line.getQuantity()
            );

            if (updatedRows == 0) {
                throw new InsufficientStockException(product.getName());
            }

            order.addItem(product, line.getQuantity());
        }

        cart.clear();

        /*
         * Saving first gives the order its database ID, which is required
         * for Stripe client_reference_id and metadata.order_id.
         *
         * If Stripe session creation throws a runtime exception, the
         * surrounding transaction rolls back the order insert, stock
         * decrements, and cart clear.
         */
        orderRepository.save(order);

        PaymentGateway.CheckoutSession checkoutSession =
                paymentGateway.createCheckoutSession(order);

        order.attachStripeSession(checkoutSession.getSessionId());
        orderRepository.save(order);

        return new CheckoutSessionResponseDTO(
                order.getId(),
                checkoutSession.getCheckoutUrl()
        );
    }
}