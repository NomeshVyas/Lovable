package com.nomesh.projects.lovable_clone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Preview {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Project project;

    String namespace;
    String podName;
    String previewUrl;

    PreviewStatus status;

    Instant startedAt;
    Instant terminatedAt;
    Instant createdAt;
}
