package com.nomesh.projects.lovable_clone.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Plan {
    Long id;

    String stripePriceId;

    Integer maxProjects;
    Integer maxTokenPerDay;
    Integer maxPreviews;

    Boolean unlimitedAi;
    Boolean active;
}
