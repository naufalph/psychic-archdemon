package com.rumantra.integration.wablas;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class WablasService {

  @Value("${wablas.token:}")
  private String token;

  private static final String BASE_URL = "https://console.wablas.com/api";
  private final RestTemplate restTemplate = new RestTemplate();

  public void sendWhatsAppMessage(String phoneNumber, String message) {
    String normalized = normalizePhone(phoneNumber);

    if (token == null || token.isBlank()) {
      log.warn("[WABLAS STUB] Would send to {}: {}", normalized, message);
      return;
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);

    Map<String, String> body = Map.of("phone", normalized, "message", message);
    HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

    try {
      restTemplate.exchange(BASE_URL + "/send-message", HttpMethod.POST, entity, String.class);
      log.info("[WABLAS] OTP sent to {}", normalized);
    } catch (Exception e) {
      log.error("[WABLAS] Failed to send message to {}: {}", normalized, e.getMessage());
      throw new RuntimeException("Gagal mengirim OTP via WhatsApp. Coba lagi.");
    }
  }

  private String normalizePhone(String phone) {
    if (phone == null) return "";
    String digits = phone.replaceAll("[^0-9]", "");
    if (digits.startsWith("0")) {
      return "62" + digits.substring(1);
    }
    if (!digits.startsWith("62")) {
      return "62" + digits;
    }
    return digits;
  }
}
