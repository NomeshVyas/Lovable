package com.nomesh.projects.lovable_clone.dto.plan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreatePlanRequest(

        @NotNull @PositiveOrZero
        Long maxPreviews,

        @NotNull @PositiveOrZero
        Long maxProjects,

        @NotNull @PositiveOrZero
        Long maxTokenPerDay,

        @NotNull
        String name,

        @NotNull
        String paymentPriceId,

        Boolean active,
        Boolean unlimitedAi
) {
}
