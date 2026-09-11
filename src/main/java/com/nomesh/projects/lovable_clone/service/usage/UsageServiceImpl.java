package com.nomesh.projects.lovable_clone.service.usage;

import com.nomesh.projects.lovable_clone.dto.plan.PlanLimitsResponse;
import com.nomesh.projects.lovable_clone.dto.usage.UsageTodayResponse;
import org.springframework.stereotype.Service;

@Service
public class UsageServiceImpl implements UsageService {
    @Override
    public UsageTodayResponse getTodayUsage(Long userId) {
        return null;
    }

    @Override
    public PlanLimitsResponse getCurrentSubscriptionLimits(Long userId) {
        return null;
    }
}
