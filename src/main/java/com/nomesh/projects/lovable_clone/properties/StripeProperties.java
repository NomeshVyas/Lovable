package com.nomesh.projects.lovable_clone.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "stripe")
public record StripeProperties(
        String apiSecretKey,
        String webhookSecretKey
) {
}
