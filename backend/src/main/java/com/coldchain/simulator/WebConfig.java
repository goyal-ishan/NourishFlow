package com.coldchain.simulator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Normalize: allow multiple origins separated by comma (NO spaces)
        String[] origins = Arrays.stream(frontendUrl.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        boolean hasWildcard = Arrays.stream(origins).anyMatch(s -> s.contains("*"));

        // If there's any wildcard, use allowedOriginPatterns (supports wildcards).
        // If explicit origins only, use allowedOrigins.
        if (hasWildcard) {
            log.info("CORS configured with wildcard patterns: using allowedOriginPatterns. Patterns: {}", String.join(",", origins));
            registry.addMapping("/**")
                    .allowedOriginPatterns(origins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
        } else {
            log.info("CORS configured with explicit allowedOrigins: {}", String.join(",", origins));
            registry.addMapping("/**")
                    .allowedOrigins(origins)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
        }
    }
}