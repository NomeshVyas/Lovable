package com.nomesh.projects.lovable_clone.entity;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Subscription {
    Long id;

    User user;

    Plan plan;

    SubscriptionStatus status;

    String stripeSubscriptionId;

    Boolean cancelAtPeriodEnd = false;

    Instant currentPeriodStart;
    Instant currentPeriodEnd;
    Instant createdAt;
    Instant updatedAt;
}
