package com.nomesh.projects.lovable_clone.service.ai;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class AiGenerationServiceImpl implements AiGenerationService {

    @Override
    public Flux<String> streamResponse(String message, Long projectId) {
        return null;
    }
}
