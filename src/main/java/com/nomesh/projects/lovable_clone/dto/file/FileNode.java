package com.nomesh.projects.lovable_clone.dto.file;

import java.time.Instant;

public record FileNode(
        String path,
        Long size,
        String type,
        Instant modifiedAt
) {
}
