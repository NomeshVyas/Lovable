package com.nomesh.projects.lovable_clone.service.ai;

import com.nomesh.projects.lovable_clone.entity.AiGenerationRun;
import com.nomesh.projects.lovable_clone.llm.parser.FileBlockParser;
import com.nomesh.projects.lovable_clone.llm.prompt.PromptUtils;
import com.nomesh.projects.lovable_clone.security.AuthUtil;
import com.nomesh.projects.lovable_clone.service.file.ProjectFileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AiGenerationServiceImpl implements AiGenerationService {

    private static final int MAX_LEGS = 6;

    ChatClient chatClient;
    AuthUtil authUtil;
    ProjectFileService projectFileService;
    AiGenerationRunService aiGenerationRunService;

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<String> streamResponse(String userMessage, Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        Map<String, Object> advisorParams = createAdvisorParams(userId, projectId);

        createChatSessionIfNotExist(projectId, userId);

        AiGenerationRun run = aiGenerationRunService.createRun(projectId, userId, userMessage);
        Long runId = run.getId();

        PersistingOutputListener listener = new PersistingOutputListener(projectFileService, aiGenerationRunService, runId, projectId);

        aiGenerationRunService.markRunning(runId);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        return streamLeg(userMessage, runId, listener, 0, advisorParams)
                .doOnError(failure :: set)
                .doFinally(signalType -> completeGeneration(listener, runId, signalType, failure.get()));
    }

    private void createChatSessionIfNotExist(Long projectId, Long userId) {

    }

    private Flux<String> streamLeg(String userMessage, Long runId, PersistingOutputListener listener, int leg, Map<String, Object> advisorParams) {
        if (leg >= MAX_LEGS) {
            log.warn("Reached max legs ({}) for runId {}", MAX_LEGS, runId);
            return Flux.empty();
        }

        int filesReachedBefore = listener.getCompletedFilePaths().size();
        FileBlockParser fileBlockParser = new FileBlockParser(listener);

        String finalPromptToAI = leg == 0 ? userMessage : PromptUtils.continuationMessage(userMessage, listener.getCompletedFilePaths());

        return chatClient.prompt()
                .system(PromptUtils.codeGenerationSystemPrompt())
                .user(finalPromptToAI)
                .advisors(advisorSpec -> advisorSpec.params(advisorParams))
                .stream()
                .chatResponse()
                .mapNotNull(this::extractText)
                .filter(text -> !text.isEmpty())
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(fileBlockParser::parse)
                .onErrorResume(error -> {
                    log.warn("Leg {} cut short for runId {}: {}", leg, runId, error.toString());
                    return Flux.empty();
                })
                .doFinally(signalType -> fileBlockParser.finish())
                .concatWith(Flux.defer(() -> {

                        boolean madeProgress = listener.getCompletedFilePaths().size() > filesReachedBefore;

                        if (listener.isModelReportedCompletion() || !madeProgress)
                            return Flux.empty();

                        return streamLeg(userMessage, runId, listener,leg + 1, advisorParams);
                }));

    }

    private Map<String, Object> createAdvisorParams(Long userId, Long projectId) {
        return Map.of(
                "userId", userId,
                "projectId", projectId
        );
    }

    private String extractText(ChatResponse chatResponse) {
        Generation result = chatResponse.getResult();
        return result == null ? null : result.getOutput().getText();
    }

    private void completeGeneration(PersistingOutputListener listener, Long runId, SignalType signalType, Throwable failure) {
        listener.flushMessageBuffer();

        switch (signalType) {
            case SignalType.ON_COMPLETE -> {
                if (listener.isModelReportedCompletion())
                    aiGenerationRunService.completeRun(runId);
                else
                    aiGenerationRunService.failRun(runId, "Generation stopped after " + MAX_LEGS + " attempts without completing");
            }
            case SignalType.CANCEL -> aiGenerationRunService.cancelRun(runId);
            case SignalType.ON_ERROR -> aiGenerationRunService.failRun(runId, String.valueOf(failure));
            default -> log.warn("Unexpected terminal signal {} for runId {}", signalType, runId);
        }
    }
}
