package com.nomesh.projects.lovable_clone.dto.plan;

public record PlanLimitsResponse(
    String planName,
    Long maxTokensPerDay,
    Integer maxProjects,
    Boolean unlimitedAi
) {
}
