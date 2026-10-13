package dev.tchiwara.ecommerce.api.payment;

import dev.tchiwara.ecommerce.api.order.Order;

public interface PaymentGateway {

    CheckoutSession createCheckoutSession(Order order);

    void expireCheckoutSession(String stripeSessionId);

    final class CheckoutSession {

        private final String sessionId;
        private final String checkoutUrl;

        public CheckoutSession(
                String sessionId,
                String checkoutUrl
        ) {
            this.sessionId = sessionId;
            this.checkoutUrl = checkoutUrl;
        }

        public String getSessionId() {
            return sessionId;
        }

        public String getCheckoutUrl() {
            return checkoutUrl;
        }
    }
}