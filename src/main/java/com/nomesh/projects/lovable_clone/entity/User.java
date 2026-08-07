package com.nomesh.projects.lovable_clone.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    Long id;

    String name;
    String email;
    String passwordHash;

    String avatarUrl;

    Instant createdAt;
    Instant updatedAt;
    Instant deletedAt;
}
