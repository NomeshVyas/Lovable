package com.nomesh.projects.lovable_clone.controller.billing;

import com.nomesh.projects.lovable_clone.dto.plan.PlanResponse;
import com.nomesh.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.nomesh.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.nomesh.projects.lovable_clone.dto.subscription.PortalResponse;
import com.nomesh.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.nomesh.projects.lovable_clone.service.PlanService;
import com.nomesh.projects.lovable_clone.service.SubscriptionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class BillingController {

    PlanService planService;
    SubscriptionService subscriptionService;

    @GetMapping("/plans")
    public ResponseEntity<List<PlanResponse>> getAllActivePlans() {
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    @GetMapping("/me/subscription")
    public ResponseEntity<SubscriptionResponse> getMySubscription() {
        Long userId = 1L;
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription(userId));
    }

    @PostMapping("/stripe/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@RequestBody CheckoutRequest request) {
        Long userId = 1L;
        return ResponseEntity.ok(subscriptionService.createCheckoutSessionUrl(request, userId));
    }

    @PostMapping("/stripe/portal")
    public ResponseEntity<PortalResponse> openCustomerPortal() {
        Long userId = 1L;
        return ResponseEntity.ok(subscriptionService.openCustomerPortal(userId));
    }
}
