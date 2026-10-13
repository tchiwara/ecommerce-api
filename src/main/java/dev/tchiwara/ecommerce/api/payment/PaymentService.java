package dev.tchiwara.ecommerce.api.payment;

import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import dev.tchiwara.ecommerce.api.order.Order;
import dev.tchiwara.ecommerce.api.order.OrderPaidEvent;
import dev.tchiwara.ecommerce.api.order.OrderRepository;
import dev.tchiwara.ecommerce.api.order.OrderService;
import dev.tchiwara.ecommerce.api.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String CHECKOUT_SESSION_COMPLETED =
            "checkout.session.completed";

    private static final String CHECKOUT_SESSION_EXPIRED =
            "checkout.session.expired";

    private static final String PAYMENT_INTENT_PAYMENT_FAILED =
            "payment_intent.payment_failed";

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void handleEvent(Event event) {
        String eventId = event.getId();
        String eventType = event.getType();

        log.info(
                "Received Stripe event: id={}, type={}",
                eventId,
                eventType
        );

        switch (eventType) {
            case CHECKOUT_SESSION_COMPLETED ->
                    handleCheckoutSessionCompleted(event);

            case CHECKOUT_SESSION_EXPIRED ->
                    handleCheckoutSessionExpired(event);

            case PAYMENT_INTENT_PAYMENT_FAILED ->
                    handlePaymentIntentPaymentFailed(event);

            default ->
                    log.info(
                            "Ignoring unhandled Stripe event: id={}, type={}",
                            eventId,
                            eventType
                    );
        }
    }

    private void handleCheckoutSessionCompleted(Event event) {
        Session session = deserialize(event, Session.class);

        Optional<Order> orderOptional = resolveOrderFromSession(session);

        if (orderOptional.isEmpty()) {
            log.warn(
                    "Stripe Checkout completion could not resolve an order: " +
                            "eventId={}, type={}, sessionId={}",
                    event.getId(),
                    event.getType(),
                    session.getId()
            );
            return;
        }

        Order order = orderOptional.get();

        if (order.getStatus() == OrderStatus.PAID) {
            log.info(
                    "Stripe Checkout completion is already processed: " +
                            "eventId={}, type={}, orderId={}, sessionId={}",
                    event.getId(),
                    event.getType(),
                    order.getId(),
                    session.getId()
            );
            return;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn(
                    "Stripe Checkout completed for a non-pending order; " +
                            "manual refund review may be required: " +
                            "eventId={}, type={}, orderId={}, orderStatus={}, sessionId={}",
                    event.getId(),
                    event.getType(),
                    order.getId(),
                    order.getStatus(),
                    session.getId()
            );
            return;
        }

        if (!isCurrentSession(order, session.getId())) {
            log.warn(
                    "Stripe Checkout completed for a stale session: " +
                            "eventId={}, type={}, orderId={}, storedSessionId={}, receivedSessionId={}",
                    event.getId(),
                    event.getType(),
                    order.getId(),
                    order.getStripeSessionId(),
                    session.getId()
            );
            return;
        }

        order.markPaid(
                session.getId(),
                session.getPaymentIntent()
        );

        orderRepository.save(order);

        eventPublisher.publishEvent(
                new OrderPaidEvent(order.getId())
        );

        log.info(
                "Order marked as paid: eventId={}, type={}, orderId={}, sessionId={}",
                event.getId(),
                event.getType(),
                order.getId(),
                session.getId()
        );
    }

    private void handleCheckoutSessionExpired(Event event) {
        Session session = deserialize(event, Session.class);

        Optional<Order> orderOptional = resolveOrderFromSession(session);

        if (orderOptional.isEmpty()) {
            log.warn(
                    "Stripe Checkout expiration could not resolve an order: " +
                            "eventId={}, type={}, sessionId={}",
                    event.getId(),
                    event.getType(),
                    session.getId()
            );
            return;
        }

        Order order = orderOptional.get();

        if (order.getStatus() != OrderStatus.PENDING) {
            log.info(
                    "Ignoring expiration for non-pending order: " +
                            "eventId={}, type={}, orderId={}, orderStatus={}, sessionId={}",
                    event.getId(),
                    event.getType(),
                    order.getId(),
                    order.getStatus(),
                    session.getId()
            );
            return;
        }

        if (!isCurrentSession(order, session.getId())) {
            log.warn(
                    "Ignoring expiration for stale Stripe session: " +
                            "eventId={}, type={}, orderId={}, storedSessionId={}, receivedSessionId={}",
                    event.getId(),
                    event.getType(),
                    order.getId(),
                    order.getStripeSessionId(),
                    session.getId()
            );
            return;
        }

        orderService.expireOrderAndRestoreStock(order);

        log.info(
                "Order expired and reserved stock restored: " +
                        "eventId={}, type={}, orderId={}, sessionId={}",
                event.getId(),
                event.getType(),
                order.getId(),
                session.getId()
        );
    }

    private void handlePaymentIntentPaymentFailed(Event event) {
        PaymentIntent paymentIntent =
                deserialize(event, PaymentIntent.class);

        String orderId = extractOrderId(paymentIntent.getMetadata());

        if (orderId == null || orderId.isBlank()) {
            orderId = resolveOrderIdFromPaymentIntent(paymentIntent)
                    .map(order -> order.getId().toString())
                    .orElse(null);
        }

        log.info(
                "Stripe payment attempt failed; order unchanged: " +
                        "eventId={}, type={}, orderId={}",
                event.getId(),
                event.getType(),
                orderId
        );
    }

    private Optional<Order> resolveOrderFromSession(Session session) {
        String orderId = extractOrderId(session.getMetadata());

        if (orderId == null || orderId.isBlank()) {
            orderId = session.getClientReferenceId();
        }

        if (orderId != null && !orderId.isBlank()) {
            try {
                return orderRepository.findById(
                        Long.valueOf(orderId)
                );
            } catch (NumberFormatException exception) {
                log.warn(
                        "Stripe event contained an invalid order ID: " +
                                "orderId={}, sessionId={}",
                        orderId,
                        session.getId()
                );
            }
        }

        if (session.getId() == null || session.getId().isBlank()) {
            return Optional.empty();
        }

        return orderRepository.findByStripeSessionId(
                session.getId()
        );
    }

    private Optional<Order> resolveOrderIdFromPaymentIntent(
            PaymentIntent paymentIntent
    ) {
        if (paymentIntent.getId() == null
                || paymentIntent.getId().isBlank()) {
            return Optional.empty();
        }

        return orderRepository.findByStripePaymentIntentId(
                paymentIntent.getId()
        );
    }

    private String extractOrderId(Map<String, String> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        return metadata.get("order_id");
    }

    private boolean isCurrentSession(
            Order order,
            String receivedSessionId
    ) {
        return order.getStripeSessionId() != null
                && order.getStripeSessionId().equals(receivedSessionId);
    }

    private <T extends StripeObject> T deserialize(
            Event event,
            Class<T> expectedType
    ) {
        EventDataObjectDeserializer deserializer =
                event.getDataObjectDeserializer();

        StripeObject stripeObject;

        try {
            stripeObject = deserializer
                    .getObject()
                    .orElseGet(() -> {
                        try {
                            return deserializer.deserializeUnsafe();
                        } catch (EventDataObjectDeserializationException exception) {
                            throw new StripePaymentException(
                                    "Unable to deserialize Stripe event data",
                                    exception
                            );
                        }
                    });
        } catch (StripePaymentException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new StripePaymentException(
                    "Unable to deserialize Stripe event data",
                    exception
            );
        }

        if (!expectedType.isInstance(stripeObject)) {
            throw new StripePaymentException(
                    "Unexpected Stripe event object type for event "
                            + event.getType()
            );
        }

        return expectedType.cast(stripeObject);
    }
}