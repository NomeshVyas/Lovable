package com.nomesh.projects.lovable_clone.service.ai;

import com.nomesh.projects.lovable_clone.entity.AiGenerationEventType;
import com.nomesh.projects.lovable_clone.llm.parser.ParsedOutputListener;
import com.nomesh.projects.lovable_clone.service.file.ProjectFileService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersistingOutputListener implements ParsedOutputListener {

    private static final int MESSAGE_FLUSH_THRESHOLD = 200;

    final ProjectFileService projectFileService;
    final AiGenerationRunService aiGenerationRunService;

    final Long runId;
    final Long projectId;

    final StringBuilder messageBuffer = new StringBuilder();
    @Getter
    final Set<String> completedFilePaths = new LinkedHashSet<>();
    @Getter
    boolean modelReportedCompletion;

    @Override
    public void onFileCompleted(String filePath, String fileContent) {
        flushMessageBuffer();
        projectFileService.saveFile(projectId, filePath, fileContent);
        aiGenerationRunService.appendEvent(runId, AiGenerationEventType.FILE_COMPLETED, filePath);
        completedFilePaths.add(filePath);
    }

    @Override
    public void onMessageText(String text) {
        messageBuffer.append(text);
        if (!modelReportedCompletion && messageBuffer.indexOf("phase=\"completed\"") >= 0)
            modelReportedCompletion = true;

        if (messageBuffer.length() >= MESSAGE_FLUSH_THRESHOLD)
            flushMessageBuffer();
    }

    @Override
    public void onIncompleteFile(String filePath) {
        flushMessageBuffer();
        log.warn("Stream ended inside an incomplete file for runId {}: {}", runId, filePath);
        aiGenerationRunService.appendEvent(runId, AiGenerationEventType.FILE_INCOMPLETE, filePath);
    }

    public void flushMessageBuffer() {
        if (messageBuffer.isEmpty()) return;

        aiGenerationRunService.appendEvent(runId, AiGenerationEventType.MESSAGE_TEXT, messageBuffer.toString());
        messageBuffer.setLength(0);
    }
}
