package com.nomesh.projects.lovable_clone.entity;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Entity
public class ChatSession {
    Project project;

    User user;

    String title;

    Instant createdAt;
    Instant updatedAt;
    Instant deletedAt;
}
