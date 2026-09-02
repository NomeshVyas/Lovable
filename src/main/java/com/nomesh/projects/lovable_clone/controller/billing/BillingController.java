package com.nomesh.projects.lovable_clone.controller.billing;

import com.nomesh.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.nomesh.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.nomesh.projects.lovable_clone.dto.subscription.PortalResponse;
import com.nomesh.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.nomesh.projects.lovable_clone.service.PaymentProcessorService;
import com.nomesh.projects.lovable_clone.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class BillingController {

    SubscriptionService subscriptionService;
    PaymentProcessorService paymentProcessor;

    @GetMapping("/me/subscription")
    public ResponseEntity<SubscriptionResponse> getMySubscription() {
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription());
    }

    @PostMapping("/payments/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(paymentProcessor.createCheckoutSessionUrl(request));
    }

    @PostMapping("/payments/portal")
    public ResponseEntity<PortalResponse> openCustomerPortal() {
        return ResponseEntity.ok(paymentProcessor.openCustomerPortal());
    }
}
