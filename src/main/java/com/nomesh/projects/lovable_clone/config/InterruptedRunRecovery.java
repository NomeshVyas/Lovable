package com.nomesh.projects.lovable_clone.config;

import com.nomesh.projects.lovable_clone.service.ai.AiGenerationRunService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class InterruptedRunRecovery implements ApplicationRunner {

    AiGenerationRunService aiGenerationRunService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        int count = aiGenerationRunService.failInterruptedRuns();
        log.info("Marked {} interrupted generation runs as FAILED", count);
    }
}
