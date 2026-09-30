package com.nomesh.projects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Immutable
@Entity
@Table(
        name = "ai_generation_events",
        uniqueConstraints = @UniqueConstraint(name = "uq_ai_generation_events_run_sequence_number", columnNames = {"run_id", "sequence_number"})
)
public class AiGenerationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    AiGenerationRun run;

    @Column(nullable = false)
    Long sequenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    AiGenerationEventType type;

    @Column(columnDefinition = "TEXT")
    String payload;

    @CreationTimestamp
    Instant createdAt;
}
