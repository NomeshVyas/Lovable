package com.nomesh.projects.lovable_clone.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.model.openai.autoconfigure.OpenAiCommonProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OpenAiConfigProbe implements ApplicationRunner {

    OpenAiCommonProperties openAiCommonProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info(">>> OpenAI timeout={} maxRetries={} baseUrl={}",
                openAiCommonProperties.getTimeout(),
                openAiCommonProperties.getMaxRetries(),
                openAiCommonProperties.getBaseUrl());
    }
}
