package com.nomesh.projects.lovable_clone.service;

import com.stripe.model.StripeObject;

import java.util.Map;

public interface PaymentEventHandlerService {

    void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata);
}
