package com.example.sportbooking.khqr.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhqrPayload {
    private BigDecimal amount;
    private String currency; // "USD" or "KHR"
    private String billNumber;
    private String storeLabel;
    private String terminalLabel;
    private String mobileNumber;
}

