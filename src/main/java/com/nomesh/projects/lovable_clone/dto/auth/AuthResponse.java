package com.nomesh.projects.lovable_clone.dto.auth;

public record AuthResponse(
        String token,
        UserProfileResponse userProfileResponse
) {
}
