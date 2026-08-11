package com.nomesh.projects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
//@Entity
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
public class ChatMessage {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

//    @ManyToOne
//    @JoinColumn(name = "chat_session")
    ChatSession chatSession;

    MessageRole role;

    String content;
    String toolCalls; // Json Array

    Integer tokensUsed;

    Instant createdAt;
}
