package com.nomesh.projects.lovable_clone.dto.auth;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String avatarUrl
) {
}
