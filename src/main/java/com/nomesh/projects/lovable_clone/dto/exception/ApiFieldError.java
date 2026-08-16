package com.nomesh.projects.lovable_clone.dto.exception;

public record ApiFieldError(
        String field,
        String message
) {
}
