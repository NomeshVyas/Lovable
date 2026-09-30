package com.nomesh.projects.lovable_clone.service.ai;

import com.nomesh.projects.lovable_clone.entity.AiGenerationEvent;
import com.nomesh.projects.lovable_clone.entity.AiGenerationEventType;
import com.nomesh.projects.lovable_clone.entity.AiGenerationRun;

public interface AiGenerationRunService {

    AiGenerationRun createRun(Long projectId, Long userId, String userMessage);

    AiGenerationEvent appendEvent(Long runId, AiGenerationEventType type, String payload);

    void markRunning(Long runId);

    void completeRun(Long runId);

    void failRun(Long runId, String errorMessage);

    void cancelRun(Long runId);

    int failInterruptedRuns();
}
