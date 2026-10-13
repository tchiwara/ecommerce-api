package dev.tchiwara.ecommerce.api.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidEmailListener {

    private final OrderRepository orderRepository;
    private final OrderEmailService orderEmailService;

    @Async
    @Transactional(
            propagation = Propagation.REQUIRES_NEW
    )
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void onOrderPaid(OrderPaidEvent event) {
        var orderOptional = orderRepository.findByIdWithItemsAndUser(
                event.orderId()
        );

        if (orderOptional.isEmpty()) {
            log.error(
                    "Cannot send order confirmation email because order was not found: orderId={}",
                    event.orderId()
            );
            return;
        }

        Order order = orderOptional.get();

        if (order.getConfirmationEmailSentAt() != null) {
            log.info(
                    "Order confirmation email already sent: orderId={}",
                    event.orderId()
            );
            return;
        }

        try {
            orderEmailService.sendConfirmationEmail(order);
            orderRepository.save(order);

            log.info(
                    "Order confirmation email processing completed: orderId={}",
                    event.orderId()
            );
        } catch (Exception exception) {
            log.error(
                    "Failed to send order confirmation email: orderId={}",
                    event.orderId(),
                    exception
            );
        }
    }
}