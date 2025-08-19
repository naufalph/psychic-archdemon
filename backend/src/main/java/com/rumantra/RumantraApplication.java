package com.rumantra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.rumantra.*.domain")
@EnableJpaRepositories(basePackages = "com.rumantra.*.repository")
public class RumantraApplication {

  public static void main(String[] args) {
    SpringApplication.run(RumantraApplication.class, args);
  }
}
