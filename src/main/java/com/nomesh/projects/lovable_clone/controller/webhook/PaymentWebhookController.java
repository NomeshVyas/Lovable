package com.nomesh.projects.lovable_clone.controller.webhook;

import com.nomesh.projects.lovable_clone.config.StripeProperties;
import com.nomesh.projects.lovable_clone.service.PaymentProcessor;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentWebhookController {

    PaymentProcessor paymentProcessor;
    StripeProperties stripeProperties;

    @PostMapping("/payment")
    public ResponseEntity<String> handlePaymentWebhooks(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signatureHeader
    ) {
        try {
            Event event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.webhookSecretKey());

            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
            StripeObject stripeObject = null;

            if (deserializer.getObject().isPresent()) {
                stripeObject = deserializer.getObject().get();
            } else {
                // Fallback: Deserialize from raw JSON
                try {
                    stripeObject = deserializer.deserializeUnsafe();
                    if (stripeObject == null) {
                        log.warn("Failed to deserialize webhook object for event: {}", event.getType());
                        return ResponseEntity.ok().build();
                    }
                } catch (Exception exception) {
                    log.error("Unsafe deserialization failed for event {}", event.getType(), exception);
                    return ResponseEntity.ok().build();
                }
            }

            Map<String, String> metadata = new HashMap<>();
            if (stripeObject instanceof Session session)
                metadata = session.getMetadata();

            paymentProcessor.handleWebhookEvent(event.getType(), stripeObject, metadata);
            return ResponseEntity.ok().build();

        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature", e);
            return ResponseEntity.badRequest().body("Invalid signature");
        }
    }
}
