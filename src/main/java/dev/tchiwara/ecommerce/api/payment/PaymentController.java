package dev.tchiwara.ecommerce.api.payment;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import dev.tchiwara.ecommerce.api.config.StripeConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final StripeConfig stripeConfig;
    private final PaymentService paymentService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader(
                    name = "Stripe-Signature",
                    required = false
            )
            String signatureHeader
    ) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        final Event event;

        try {
            event = Webhook.constructEvent(
                    payload,
                    signatureHeader,
                    stripeConfig.getWebhookSecret()
            );
        } catch (SignatureVerificationException exception) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }

        /*
         * Exceptions from paymentService intentionally propagate.
         * In particular, optimistic-lock failures must produce a 5xx
         * response so Stripe retries the webhook.
         */
        paymentService.handleEvent(event);

        return ResponseEntity.ok().build();
    }
}