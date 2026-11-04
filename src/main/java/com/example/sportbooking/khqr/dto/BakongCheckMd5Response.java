package com.example.sportbooking.khqr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BakongCheckMd5Response {

    private Integer responseCode;
    private String responseMessage;
    private TransactionData data;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransactionData {
        private String hash;
        private String fromAccountId;
        private String toAccountId;
        private BigDecimal amount;
        private String currency;
        private String description;
        private Long acknowledgedDate;
    }
}

