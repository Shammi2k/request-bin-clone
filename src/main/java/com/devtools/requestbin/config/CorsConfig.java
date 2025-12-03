package com.devtools.requestbin.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS (Cross-Origin Resource Sharing) configuration
 * <p>
 * Why CORS matters:
 * - Browser security prevents  (<a href="http://localhost:3000">frontend</a>) from calling
 * (<a href="http://localhost:8080">backend</a>) without explicit permission
 * - This config grants that permission
 */
@Configuration
public class CorsConfig
{

  @Bean
  public CorsFilter corsFilter()
  {
    CorsConfiguration config = new CorsConfiguration();

    // Allow requests from these origins
    config.setAllowedOrigins(Arrays.asList(
      "http://localhost:3000",     // React dev server
      "http://localhost:5173",     // Vite dev server
      "http://localhost:8080",     // Same origin (for testing)
      "http://127.0.0.1:8080"
    ));

    // Allow these HTTP methods
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

    // Allow these headers
    config.setAllowedHeaders(List.of("*"));

    // Allow credentials (cookies, auth headers)
    config.setAllowCredentials(true);

    // How long browser can cache CORS response (1 hour)
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);  // Apply to all endpoints

    return new CorsFilter(source);
  }
}