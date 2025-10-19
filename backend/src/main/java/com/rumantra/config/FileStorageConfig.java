package com.rumantra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Configuration for file storage. Exposes local uploads directory for serving static files. */
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Serve local uploads via /uploads/** URL path
    registry.addResourceHandler("/uploads/**").addResourceLocations("file:uploads/");
  }
}
