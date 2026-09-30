package com.nomesh.projects.lovable_clone.service.ai;

import com.nomesh.projects.lovable_clone.entity.*;
import com.nomesh.projects.lovable_clone.exception.BadRequestException;
import com.nomesh.projects.lovable_clone.repository.AiGenerationEventRepository;
import com.nomesh.projects.lovable_clone.repository.AiGenerationRunRepository;
import com.nomesh.projects.lovable_clone.repository.ProjectRepository;
import com.nomesh.projects.lovable_clone.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AiGenerationRunServiceImpl implements AiGenerationRunService{

    AiGenerationRunRepository runRepository;
    AiGenerationEventRepository eventRepository;
    UserRepository userRepository;
    ProjectRepository projectRepository;

    @Override
    public AiGenerationRun createRun(Long projectId, Long userId, String userMessage) {
        User user = userRepository.getReferenceById(userId);
        Project project = projectRepository.getReferenceById(projectId);

        runRepository.findFirstByProjectIdAndStatusIn(projectId, List.of(AiGenerationRunStatus.QUEUED, AiGenerationRunStatus.RUNNING))
                .ifPresent(existing -> {
                    throw new BadRequestException("A ai generation is already running for project id: " + projectId);
                });

        AiGenerationRun run = AiGenerationRun.builder()
                .project(project)
                .requestedBy(user)
                .userMessage(userMessage)
                .build();

        return runRepository.save(run);
    }

    @Override
    public AiGenerationEvent appendEvent(Long runId, AiGenerationEventType type, String payload) {
        AiGenerationRun run = runRepository.findByIdOrThrow(runId);

        long nextSequence = run.getLastEventSequenceNumber() + 1;

        run.setLastEventSequenceNumber(nextSequence);
        runRepository.save(run);

        AiGenerationEvent event = AiGenerationEvent.builder()
                .run(run)
                .sequenceNumber(nextSequence)
                .type(type)
                .payload(payload)
                .build();

        return eventRepository.save(event);
    }

    @Override
    public void markRunning(Long runId) {
        AiGenerationRun run = runRepository.findByIdOrThrow(runId);
        run.setStatus(AiGenerationRunStatus.RUNNING);
        run.setStartedAt(Instant.now());

        runRepository.save(run);
        appendEvent(runId, AiGenerationEventType.RUN_STARTED, null);
    }

    @Override
    public void completeRun(Long runId) {
        finishRun(runId, AiGenerationRunStatus.SUCCEEDED, null);
        appendEvent(runId, AiGenerationEventType.RUN_COMPLETED, null);
    }

    @Override
    public void failRun(Long runId, String errorMessage) {
        finishRun(runId, AiGenerationRunStatus.FAILED, errorMessage);
        appendEvent(runId, AiGenerationEventType.RUN_FAILED, errorMessage);
    }

    @Override
    public void cancelRun(Long runId) {
        finishRun(runId, AiGenerationRunStatus.CANCELLED, null);
        appendEvent(runId, AiGenerationEventType.RUN_CANCELLED, null);
    }

    @Override
    public int failInterruptedRuns() {
        List<AiGenerationRun> interrupted = runRepository.findByStatusIn(List.of(AiGenerationRunStatus.QUEUED, AiGenerationRunStatus.RUNNING));

        interrupted.forEach(run -> {
            run.setStatus(AiGenerationRunStatus.FAILED);
            run.setErrorMessage("Interrupted by application restart");
            run.setFinishedAt(Instant.now());
        });

        runRepository.saveAll(interrupted);
        return interrupted.size();
    }

    private void finishRun(Long runId, AiGenerationRunStatus status, String errorMessage) {
        AiGenerationRun run = runRepository.findByIdOrThrow(runId);

        if (run.getStatus().isTerminal()) {
            log.debug("Run {} is already {}, ignoring transition to {}", runId, run.getStatus(), status);
            return;
        }

        run.setStatus(status);
        run.setErrorMessage(errorMessage);
        run.setFinishedAt(Instant.now());

        runRepository.save(run);
    }
}
