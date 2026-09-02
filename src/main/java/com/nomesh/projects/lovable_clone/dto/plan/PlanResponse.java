package com.nomesh.projects.lovable_clone.dto.plan;

public record PlanResponse(
        Long id,
        String name,
        Long maxProjects,
        Long maxTokenPerDay,
        Long maxPreviews,
        Boolean unlimitedAi
) {
}
