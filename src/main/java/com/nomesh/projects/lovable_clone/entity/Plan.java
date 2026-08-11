package com.nomesh.projects.lovable_clone.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    int price;

    String stripePriceId;

    Integer maxProjects;
    Integer maxTokenPerDay;
    Integer maxPreviews;

    Boolean unlimitedAi;
    Boolean active;
}
