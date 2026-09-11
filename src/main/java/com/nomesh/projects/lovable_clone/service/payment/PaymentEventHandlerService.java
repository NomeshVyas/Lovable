package com.nomesh.projects.lovable_clone.service.payment;

public interface PaymentEventHandlerService {

    void handleWebhookEvent(String payload, String signatureHeader);
}
