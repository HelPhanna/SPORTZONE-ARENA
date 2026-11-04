package com.example.sportbooking.khqr.client;

import com.example.sportbooking.config.KhqrProperties;
import com.example.sportbooking.khqr.dto.BakongCheckMd5Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
public class BakongApiClient {

    private final KhqrProperties properties;
    private final RestClient restClient;

    public BakongApiClient(KhqrProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.getApiUrl() != null ? properties.getApiUrl() : "https://api-bakong.nbc.gov.kh/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Checks with NBC Bakong if the transaction with the given MD5 hash was completed.
     *
     * @param md5 The MD5 hash of the KHQR string
     * @return true if successfully settled in Bakong, false otherwise
     */
    public boolean checkTransactionByMd5(String md5) {
        if (properties.isMockMode()) {
            log.debug("Bakong is in MOCK mode. Returning false for auto-poll until manual simulation.");
            return false;
        }

        if (properties.getToken() == null || properties.getToken().isBlank()) {
            log.warn("Bakong API Token is missing. Please set bakong.token in application.properties.");
            return false;
        }

        try {
            BakongCheckMd5Response response = restClient.post()
                    .uri("/check_transaction_by_md5")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getToken().trim())
                    .body(Map.of("md5", md5))
                    .retrieve()
                    .body(BakongCheckMd5Response.class);

            if (response != null && Integer.valueOf(0).equals(response.getResponseCode())) {
                log.info("Bakong payment confirmed for MD5: {}, amount: {}", 
                        md5, response.getData() != null ? response.getData().getAmount() : "unknown");
                return true;
            }
        } catch (Exception e) {
            log.error("Failed to query Bakong Open API for MD5 {}: {}", md5, e.getMessage());
        }

        return false;
    }
}

