package com.rumantra.shared.email;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class EmailTemplateService {

  private static final String TEMPLATE_BASE_PATH = "templates/email/";

  public String render(String templateName, Map<String, String> variables) {
    try {
      String layout = loadResource(TEMPLATE_BASE_PATH + "layout.html");
      String content = loadResource(TEMPLATE_BASE_PATH + templateName + ".html");
      String merged = layout.replace("{{CONTENT}}", content);
      return replacePlaceholders(merged, variables);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load email template: " + templateName, e);
    }
  }

  private String loadResource(String path) throws IOException {
    ClassPathResource resource = new ClassPathResource(path);
    return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
  }

  private String replacePlaceholders(String template, Map<String, String> variables) {
    String result = template;
    for (Map.Entry<String, String> entry : variables.entrySet()) {
      result =
          result.replace(
              "{{" + entry.getKey() + "}}", entry.getValue() != null ? entry.getValue() : "");
    }
    return result;
  }
}
