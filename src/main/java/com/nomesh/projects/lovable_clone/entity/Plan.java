package com.nomesh.projects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, unique = true)
    String name;

    @Column(unique = true)
    String paymentPriceId;

    @Column(nullable = false)
    Long maxProjects;
    Long maxTokenPerDay;
    Long maxPreviews;

    @Builder.Default
    Boolean unlimitedAi = false;

    @Builder.Default
    Boolean active = true;
}
