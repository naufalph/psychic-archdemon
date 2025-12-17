package com.rumantra.integration.xendit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class XenditClient {

  @Value("${xendit.api-key}")
  private String apiKey;

  private static final String BASE_URL = "https://api.xendit.co";

  private final RestTemplate restTemplate = new RestTemplate();

  public <T> T post(String endpoint, Object request, Class<T> responseType) {
    HttpHeaders headers = createHeaders();
    HttpEntity<?> entity = new HttpEntity<>(request, headers);

    try {
      ResponseEntity<T> response =
          restTemplate.exchange(BASE_URL + endpoint, HttpMethod.POST, entity, responseType);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Xendit API error: {}", e.getResponseBodyAsString());
      throw new XenditException("API call failed: " + e.getMessage(), e);
    }
  }

  public <T> T get(String endpoint, Class<T> responseType) {
    HttpHeaders headers = createHeaders();
    HttpEntity<?> entity = new HttpEntity<>(headers);

    try {
      ResponseEntity<T> response =
          restTemplate.exchange(BASE_URL + endpoint, HttpMethod.GET, entity, responseType);
      return response.getBody();
    } catch (HttpClientErrorException e) {
      log.error("Xendit API error: {}", e.getResponseBodyAsString());
      throw new XenditException("API call failed: " + e.getMessage(), e);
    }
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setBasicAuth(apiKey, "");
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
