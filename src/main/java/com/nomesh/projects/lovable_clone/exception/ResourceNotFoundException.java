package com.nomesh.projects.lovable_clone.exception;

import com.nomesh.projects.lovable_clone.entity.ProjectMemberId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ResourceNotFoundException extends RuntimeException {
    String resourceName;
    String resourceId;

    public ResourceNotFoundException(String resourceName, Long resourceId) {
        this(resourceName, resourceId.toString());
    }
    public ResourceNotFoundException(ProjectMemberId projectMemberId) {
        this("Project Member", projectMemberId.toString());
    }
}
