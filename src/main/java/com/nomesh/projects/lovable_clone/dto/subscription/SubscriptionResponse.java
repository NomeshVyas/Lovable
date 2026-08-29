package com.nomesh.projects.lovable_clone.dto.subscription;

import com.nomesh.projects.lovable_clone.dto.plan.PlanResponse;

import java.time.Instant;

public record SubscriptionResponse(
    PlanResponse plan,
    String status,
    Instant currentPeriodEnd,
    Long tokenUsedThisCycle
) {
}
