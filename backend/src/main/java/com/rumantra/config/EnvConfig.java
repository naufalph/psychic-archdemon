package com.rumantra.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig implements EnvironmentPostProcessor {

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    try {
      // Load .env file from the current working directory
      Dotenv dotenv =
          Dotenv.configure()
              .directory("./") // Maven's forked process already runs from backend/
              .ignoreIfMalformed()
              .ignoreIfMissing()
              .load();

      // Convert to Map for Spring PropertySource
      Map<String, Object> envProperties = new HashMap<>();
      dotenv
          .entries()
          .forEach(
              entry -> {
                String key = entry.getKey();
                String value = entry.getValue();

                // Only add if not already set by actual environment variables
                if (System.getenv(key) == null) {
                  envProperties.put(key, value);
                }
              });

      // Add to Spring Environment with lower priority than system env vars
      if (!envProperties.isEmpty()) {
        environment.getPropertySources().addLast(new MapPropertySource("dotenv", envProperties));
      }

    } catch (Exception e) {
      // Log warning but don't fail startup - production might use real env vars
      System.err.println("Warning: Could not load .env file: " + e.getMessage());
    }
  }
}
