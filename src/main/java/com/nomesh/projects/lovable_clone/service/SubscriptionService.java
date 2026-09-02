package com.nomesh.projects.lovable_clone.service;

import com.nomesh.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.nomesh.projects.lovable_clone.entity.SubscriptionStatus;

import java.time.Instant;

public interface SubscriptionService {

    SubscriptionResponse getCurrentSubscription();

    void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId);

    void updateSubscription(String paymentSubscriptionId, SubscriptionStatus subscriptionStatus, Instant periodStart, Instant periodEnd, Long planId, Boolean cancelAtPeriodEnd);

    void cancelSubscription(String paymentSubscriptionId);

    void renewSubscriptionPeriod(String paymentSubscriptionId, Instant periodStart, Instant periodEnd);

    void markSubscriptionPastDue(String paymentSubscriptionId);

    boolean canCreateNewProject();
}
