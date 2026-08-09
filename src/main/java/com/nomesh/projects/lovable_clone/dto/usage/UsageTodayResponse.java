package com.nomesh.projects.lovable_clone.dto.usage;

public record UsageTodayResponse(
        Long tokenUsed,
        Long tokenLimit,
        int previewRunning,
        int previewLimit
) {
}
