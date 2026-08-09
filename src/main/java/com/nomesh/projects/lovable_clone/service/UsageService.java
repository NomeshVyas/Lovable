package com.nomesh.projects.lovable_clone.service;

import com.nomesh.projects.lovable_clone.dto.plan.PlanLimitsResponse;
import com.nomesh.projects.lovable_clone.dto.usage.UsageTodayResponse;

public interface UsageService {
    UsageTodayResponse getTodayUsage(Long userId);

    PlanLimitsResponse getCurrentSubscriptionLimits(Long userId);
}
