package com.nomesh.projects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(
        name = "ai_generation_runs",
        indexes = {
                @Index(name = "idx_ai_generation_runs_project_created_desc", columnList = "project_id, created_at DESC"),
                @Index(name = "idx_ai_generation_runs_status", columnList = "status")
        }
)
public class AiGenerationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false, updatable = false)
    Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false, updatable = false)
    User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    AiGenerationRunStatus status = AiGenerationRunStatus.QUEUED;

    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    String userMessage;

    @Column(columnDefinition = "TEXT")
    String errorMessage;

    @Builder.Default
    @Column(nullable = false)
    Long lastEventSequenceNumber = 0L;

    Instant startedAt;
    Instant finishedAt;

    @CreationTimestamp
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;
}