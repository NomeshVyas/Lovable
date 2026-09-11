package com.nomesh.projects.lovable_clone.controller.webhook;

import com.nomesh.projects.lovable_clone.service.payment.PaymentEventHandlerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhooks")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class PaymentWebhookController {

    PaymentEventHandlerService paymentEventHandlerService;

    @PostMapping("/payment")
    public ResponseEntity<String> handlePaymentWebhooks(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signatureHeader
    ) {
        paymentEventHandlerService.handleWebhookEvent(payload, signatureHeader);
        return ResponseEntity.ok().build();
    }
}
