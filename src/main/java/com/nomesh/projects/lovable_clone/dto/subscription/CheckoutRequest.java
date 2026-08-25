package com.nomesh.projects.lovable_clone.dto.subscription;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CheckoutRequest(
        @NotNull @Positive
        Long planId
) {
}
