package com.example.sportbooking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "bakong")
public class KhqrProperties {

    /**
     * Bakong Account ID (e.g. your_account@aba or your_phone@bakong)
     */
    private String accountId = "sportzone@aba";

    /**
     * Merchant display name (max 25 chars)
     */
    private String merchantName = "SPORTZONE ARENA";

    /**
     * Merchant city (max 15 chars)
     */
    private String merchantCity = "Phnom Penh";

    /**
     * Merchant Category Code (MCC). Default 5999 or 7997 for Sports
     */
    private String mcc = "5999";

    /**
     * Transaction Currency: USD (840) or KHR (116)
     */
    private String currency = "USD";

    /**
     * Acquiring bank / Merchant ID for Tag 29 sub-tag 01
     */
    private String acquiringBank = "Bakong";

    /**
     * Optional bank account number
     */
    private String accountNumber;

    /**
     * Optional KHR Bank account number
     */
    private String khrAccountNumber;

    /**
     * Optional ABA user/profile ID
     */
    private String abaUserId;

    /**
     * NBC Bakong API endpoint base URL
     */
    private String apiUrl = "https://api-bakong.nbc.gov.kh/v1";

    /**
     * NBC Bakong Open API Developer Bearer JWT Token
     */
    private String token = "";

    /**
     * Mock mode for local testing without calling live Bakong API
     */
    private boolean mockMode = true;
}

