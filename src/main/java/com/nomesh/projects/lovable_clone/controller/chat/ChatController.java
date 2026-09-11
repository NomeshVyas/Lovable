package com.nomesh.projects.lovable_clone.controller.chat;

import com.nomesh.projects.lovable_clone.dto.chat.ChatRequest;
import com.nomesh.projects.lovable_clone.service.ai.AiGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@FieldDefaults(makeFinal = true)
@RequestMapping("/api/chat")
public class ChatController {

    AiGenerationService aiGenerationService;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestBody ChatRequest request) {
        return aiGenerationService.streamResponse(request.message(), request.projectId())
                .map(
                    data -> ServerSentEvent.<String>builder()
                        .data(data)
                        .build()
                );
    }
}
