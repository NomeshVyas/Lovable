package com.nomesh.projects.lovable_clone.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client")
public record ClientProperties(
        String frontendUrl
) {
}
