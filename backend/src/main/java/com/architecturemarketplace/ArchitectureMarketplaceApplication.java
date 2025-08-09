package com.architecturemarketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.architecturemarketplace.*.domain")
@EnableJpaRepositories(basePackages = "com.architecturemarketplace.*.repository")
public class ArchitectureMarketplaceApplication {

  public static void main(String[] args) {
    SpringApplication.run(ArchitectureMarketplaceApplication.class, args);
  }
}
