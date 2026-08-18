package com.nomesh.projects.lovable_clone.dto.jwt_config;

import java.util.List;

public record JwtUserPrincipal (
        Long userId,
        String email,
        String username,
        List<String> authorities
) {
}
