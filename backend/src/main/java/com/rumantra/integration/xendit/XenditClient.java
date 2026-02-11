package com.rumantra.integration.xendit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class XenditClient {

  @Value("${xendit.api-key}")
  private String apiKey;

  private static final String BASE_URL = "https://api.xendit.co";

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public <T> T post(String endpoint, Object request, Class<T> responseType) {
    HttpHeaders headers = createHeaders();
    HttpEntity<?> entity = new HttpEntity<>(request, headers);
    String fullUrl = BASE_URL + endpoint;

    try {
      log.info("=== XENDIT API REQUEST (POST) ===");
      log.info("URL: {}", fullUrl);
      log.info("Headers: {}", sanitizeHeaders(headers));
      String requestBody = objectMapper.writeValueAsString(request);
      log.info("Request Body: {}", requestBody);

      ResponseEntity<T> response =
          restTemplate.exchange(fullUrl, HttpMethod.POST, entity, responseType);

      log.info("=== XENDIT API RESPONSE (POST) ===");
      log.info("Status Code: {}", response.getStatusCode());
      log.info("Response Headers: {}", response.getHeaders());
      String responseBody = objectMapper.writeValueAsString(response.getBody());
      log.info("Response Body: {}", responseBody);

      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("=== XENDIT API ERROR (POST) ===");
      log.error("URL: {}", fullUrl);
      log.error("Status Code: {}", e.getStatusCode());
      log.error("Status Text: {}", e.getStatusText());
      log.error("Response Headers: {}", e.getResponseHeaders());
      log.error("Response Body: {}", e.getResponseBodyAsString());
      log.error("Exception Message: {}", e.getMessage());
      throw new XenditException("API call failed: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("=== XENDIT API UNEXPECTED ERROR (POST) ===");
      log.error("URL: {}", fullUrl);
      log.error("Error: {}", e.getMessage(), e);
      throw new XenditException("Unexpected error: " + e.getMessage(), e);
    }
  }

  public <T> T postV2(String endpoint, Object request, Class<T> responseType) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth(apiKey, "");
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<?> entity = new HttpEntity<>(request, headers);
    String fullUrl = BASE_URL + endpoint;

    try {
      log.info("=== XENDIT API REQUEST (POST V2) ===");
      log.info("URL: {}", fullUrl);
      log.info("Headers: {}", sanitizeHeaders(headers));
      String requestBody = objectMapper.writeValueAsString(request);
      log.info("Request Body: {}", requestBody);

      ResponseEntity<T> response =
          restTemplate.exchange(fullUrl, HttpMethod.POST, entity, responseType);

      log.info("=== XENDIT API RESPONSE (POST V2) ===");
      log.info("Status Code: {}", response.getStatusCode());
      log.info("Response Headers: {}", response.getHeaders());
      String responseBody = objectMapper.writeValueAsString(response.getBody());
      log.info("Response Body: {}", responseBody);

      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("=== XENDIT API ERROR (POST V2) ===");
      log.error("URL: {}", fullUrl);
      log.error("Status Code: {}", e.getStatusCode());
      log.error("Status Text: {}", e.getStatusText());
      log.error("Response Headers: {}", e.getResponseHeaders());
      log.error("Response Body: {}", e.getResponseBodyAsString());
      log.error("Exception Message: {}", e.getMessage());
      throw new XenditException("API call failed: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("=== XENDIT API UNEXPECTED ERROR (POST V2) ===");
      log.error("URL: {}", fullUrl);
      log.error("Error: {}", e.getMessage(), e);
      throw new XenditException("Unexpected error: " + e.getMessage(), e);
    }
  }

  public <T> T get(String endpoint, Class<T> responseType) {
    HttpHeaders headers = createHeaders();
    HttpEntity<?> entity = new HttpEntity<>(headers);
    String fullUrl = BASE_URL + endpoint;

    try {
      log.info("=== XENDIT API REQUEST (GET) ===");
      log.info("URL: {}", fullUrl);
      log.info("Headers: {}", sanitizeHeaders(headers));

      ResponseEntity<T> response =
          restTemplate.exchange(fullUrl, HttpMethod.GET, entity, responseType);

      log.info("=== XENDIT API RESPONSE (GET) ===");
      log.info("Status Code: {}", response.getStatusCode());
      log.info("Response Headers: {}", response.getHeaders());
      String responseBody = objectMapper.writeValueAsString(response.getBody());
      log.info("Response Body: {}", responseBody);

      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("=== XENDIT API ERROR (GET) ===");
      log.error("URL: {}", fullUrl);
      log.error("Status Code: {}", e.getStatusCode());
      log.error("Status Text: {}", e.getStatusText());
      log.error("Response Headers: {}", e.getResponseHeaders());
      log.error("Response Body: {}", e.getResponseBodyAsString());
      log.error("Exception Message: {}", e.getMessage());
      throw new XenditException("API call failed: " + e.getMessage(), e);
    } catch (Exception e) {
      log.error("=== XENDIT API UNEXPECTED ERROR (GET) ===");
      log.error("URL: {}", fullUrl);
      log.error("Error: {}", e.getMessage(), e);
      throw new XenditException("Unexpected error: " + e.getMessage(), e);
    }
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth(apiKey, "");
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("api-version", "2024-11-11");
    return headers;
  }

  private String sanitizeHeaders(HttpHeaders headers) {
    HttpHeaders sanitized = new HttpHeaders();
    headers.forEach(
        (key, value) -> {
          if ("Authorization".equalsIgnoreCase(key)) {
            sanitized.add(key, "[REDACTED - Length: " + value.get(0).length() + "]");
          } else {
            sanitized.addAll(key, value);
          }
        });
    return sanitized.toString();
  }
}
