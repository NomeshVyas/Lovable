package com.nomesh.projects.lovable_clone.service.ai;

import com.nomesh.projects.lovable_clone.llm.PromptUtils;
import com.nomesh.projects.lovable_clone.security.AuthUtil;
import com.nomesh.projects.lovable_clone.service.file.ProjectFileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AiGenerationServiceImpl implements AiGenerationService {

    ChatClient chatClient;
    AuthUtil authUtil;
    ProjectFileService projectFileService;

    private static final Pattern FILE_TAG_PATTERN = Pattern.compile(
            "<file path=\"([^\"]+)\">(.*?)</file>",
            Pattern.DOTALL
    );

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<String> streamResponse(String userMessage, Long projectId) {
        Long userId = authUtil.getCurrentUserId();

        createChatSessionIfNotExist(projectId, userId);
        Map<String, Object> advisorParams = Map.of(
                "userId", userId,
                "projectId", projectId
        );

        StringBuilder fullResponseBuffer = new StringBuilder();

        return chatClient.prompt()
                .system(PromptUtils.codeGenerationSystemPrompt())
                .user(userMessage)
                .advisors(advisorSpec -> {
                    advisorSpec.params(advisorParams);
                })
                .stream()
                .chatResponse()
                .doOnNext(chatResponse -> {
                    String content = Objects.requireNonNull(chatResponse.getResult()).getOutput().getText();
                    fullResponseBuffer.append(content);
                })
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(() -> {
                            parseAndSaveFiles(fullResponseBuffer.toString(), projectId);
                        });
                })
                .doOnError(error -> log.error("Error during streaming for projectId: {}", projectId))
                .map(chatResponse -> Objects.requireNonNull(chatResponse.getResult().getOutput().getText()));
    }


    private void parseAndSaveFiles(String fullResponse, Long projectId) {
//        String dummy = """
//                <message>I'm going to read the files and generate the code</message>
//                <file path="src/App.jsx">
//                    import App from './App.jsx'
//                    ......
//                </file>
//                <message>I'm going to read the files and generate the code</message>
//                <file path="src/App.jsx">
//                    import App from './App.jsx'
//                    ......
//                </file>
//                """;
        Matcher matcher = FILE_TAG_PATTERN.matcher(fullResponse);

        while (matcher.find()) {
            String filePath = matcher.group(1);
            String fileContent = matcher.group(2).trim();

            projectFileService.saveFile(projectId, filePath, fileContent);
        }
    }

    private void createChatSessionIfNotExist(Long projectId, Long userId) {

    }
}
