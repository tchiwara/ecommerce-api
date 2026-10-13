package dev.tchiwara.ecommerce.api.payment;

import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import dev.tchiwara.ecommerce.api.config.StripeConfig;
import dev.tchiwara.ecommerce.api.order.Order;
import dev.tchiwara.ecommerce.api.order.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class StripePaymentGateway implements PaymentGateway {

    private static final long CHECKOUT_SESSION_LIFETIME_MINUTES = 30L;

    private final StripeConfig stripeConfig;

    @Override
    public CheckoutSession createCheckoutSession(Order order) {
        SessionCreateParams.Builder sessionBuilder =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.PAYMENT)
                        .setClientReferenceId(order.getId().toString())
                        .setSuccessUrl(stripeConfig.getSuccessUrl())
                        .setCancelUrl(stripeConfig.getCancelUrl())
                        .setExpiresAt(
                                Instant.now()
                                        .plus(
                                                CHECKOUT_SESSION_LIFETIME_MINUTES,
                                                ChronoUnit.MINUTES
                                        )
                                        .getEpochSecond()
                        )
                        .putMetadata(
                                "order_id",
                                order.getId().toString()
                        )
                        .setPaymentIntentData(
                                SessionCreateParams.PaymentIntentData.builder()
                                        .putMetadata(
                                                "order_id",
                                                order.getId().toString()
                                        )
                                        .build()
                        );

        for (OrderItem item : order.getItems()) {
            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(item.getProductName())
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(stripeConfig.getCurrency().toLowerCase())
                            .setUnitAmount(toMinorUnits(item.getUnitPrice()))
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) item.getQuantity())
                            .setPriceData(priceData)
                            .build();

            sessionBuilder.addLineItem(lineItem);
        }

        try {
            Session session = Session.create(sessionBuilder.build());

            if (session.getId() == null || session.getUrl() == null) {
                throw new StripePaymentException(
                        "Stripe returned an incomplete Checkout Session"
                );
            }

            return new CheckoutSession(
                    session.getId(),
                    session.getUrl()
            );
        } catch (StripePaymentException exception) {
            throw exception;
        } catch (StripeException exception) {
            throw new StripePaymentException(
                    "Unable to create Stripe Checkout Session",
                    exception
            );
        }
    }

    @Override
    public void expireCheckoutSession(String stripeSessionId) {
        if (stripeSessionId == null || stripeSessionId.isBlank()) {
            return;
        }

        try {
            Session session = Session.retrieve(stripeSessionId);

            String status = session.getStatus();

            if ("expired".equalsIgnoreCase(status)
                    || "complete".equalsIgnoreCase(status)) {
                return;
            }

            session.expire();
        } catch (InvalidRequestException exception) {
            /*
             * Stripe can reject expiration when the session has already
             * expired or completed between retrieval and expiration.
             * Treat those cases as already closed.
             */
            String message = exception.getMessage();

            if (message != null
                    && (message.toLowerCase().contains("expired")
                    || message.toLowerCase().contains("complete")
                    || message.toLowerCase().contains("expire"))) {
                return;
            }

            throw new StripePaymentException(
                    "Unable to expire Stripe Checkout Session",
                    exception
            );
        } catch (StripeException exception) {
            throw new StripePaymentException(
                    "Unable to expire Stripe Checkout Session",
                    exception
            );
        }
    }

    private long toMinorUnits(BigDecimal amount) {
        return amount
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact();
    }
}