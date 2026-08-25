package com.nomesh.projects.lovable_clone.service;

import com.nomesh.projects.lovable_clone.dto.subscription.SubscriptionResponse;

public interface SubscriptionService {

    SubscriptionResponse getCurrentSubscription(Long userId);
}
