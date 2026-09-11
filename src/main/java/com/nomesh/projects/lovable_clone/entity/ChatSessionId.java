package com.nomesh.projects.lovable_clone.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Embeddable
@EqualsAndHashCode
public class ChatSessionId implements Serializable {
    Long projectId;
    Long userId;
}
