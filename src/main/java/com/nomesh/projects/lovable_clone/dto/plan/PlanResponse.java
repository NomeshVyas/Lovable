package com.nomesh.projects.lovable_clone.dto.plan;

public record PlanResponse(
        Long id,

        String stripePriceId,

        Integer maxProjects,
        Integer maxTokenPerDay,
        Integer maxPreviews,
        int price,
        Boolean unlimitedAi
) {
}
