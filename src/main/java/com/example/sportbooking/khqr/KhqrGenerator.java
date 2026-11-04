package com.example.sportbooking.khqr;

import com.example.sportbooking.config.KhqrProperties;
import com.example.sportbooking.khqr.dto.KhqrData;
import com.example.sportbooking.khqr.dto.KhqrPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class KhqrGenerator {

    private final KhqrProperties properties;

    /**
     * Generates a fully EMVCo / KHQR compliant dynamic QR string and its MD5 hash.
     */
    public KhqrData generateDynamicKhqr(KhqrPayload payload) {
        StringBuilder tlv = new StringBuilder();

        // Tag 00: Payload Format Indicator (01)
        appendTlv(tlv, "00", "01");

        // Tag 01: Point of Initiation Method (12 = Dynamic, 11 = Static)
        appendTlv(tlv, "01", "12");

        // Tag 29: Merchant Account Information (Bakong Individual / Solo Merchant)
        StringBuilder merchantAccount = new StringBuilder();
        appendTlv(merchantAccount, "00", properties.getAccountId());
        if (properties.getAccountNumber() != null && !properties.getAccountNumber().isBlank()) {
            appendTlv(merchantAccount, "01", properties.getAccountNumber());
        }
        if (properties.getAcquiringBank() != null && !properties.getAcquiringBank().isBlank()) {
            appendTlv(merchantAccount, "02", properties.getAcquiringBank());
        }
        appendTlv(tlv, "29", merchantAccount.toString());

        // Tag 40: ABA Proprietary P2P Tag (when using ABA Pay / Mobile)
        if (properties.getAbaUserId() != null && !properties.getAbaUserId().isBlank()) {
            StringBuilder abaP2p = new StringBuilder();
            appendTlv(abaP2p, "00", "abaP2P");
            appendTlv(abaP2p, "01", properties.getAbaUserId());
            if (properties.getKhrAccountNumber() != null && !properties.getKhrAccountNumber().isBlank()) {
                appendTlv(abaP2p, "02", properties.getKhrAccountNumber());
            }
            if (properties.getAccountNumber() != null && !properties.getAccountNumber().isBlank()) {
                appendTlv(abaP2p, "03", properties.getAccountNumber());
            }
            appendTlv(tlv, "40", abaP2p.toString());
        }

        // Tag 52: Merchant Category Code
        String mcc = properties.getMcc() != null && !properties.getMcc().isBlank() ? properties.getMcc() : "5999";
        appendTlv(tlv, "52", mcc);

        // Tag 53: Transaction Currency (840 = USD, 116 = KHR)
        String currencyCode = "KHR".equalsIgnoreCase(payload.getCurrency()) ? "116" : "840";
        appendTlv(tlv, "53", currencyCode);

        // Tag 54: Transaction Amount
        BigDecimal amount = payload.getAmount() != null ? payload.getAmount() : BigDecimal.ZERO;
        String formattedAmount = amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        appendTlv(tlv, "54", formattedAmount);

        // Tag 58: Country Code (KH)
        appendTlv(tlv, "58", "KH");

        // Tag 59: Merchant Name (Up to 25 characters, alphanumeric and spaces only)
        String merchantName = properties.getMerchantName() != null ? properties.getMerchantName() : "MERCHANT";
        merchantName = merchantName.replaceAll("[^a-zA-Z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        if (merchantName.isEmpty()) {
            merchantName = "MERCHANT";
        }
        if (merchantName.length() > 25) {
            merchantName = merchantName.substring(0, 25).trim();
        }
        appendTlv(tlv, "59", merchantName);

        // Tag 60: Merchant City (Up to 15 characters, alphanumeric and spaces only)
        String city = properties.getMerchantCity() != null ? properties.getMerchantCity() : "Phnom Penh";
        city = city.replaceAll("[^a-zA-Z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        if (city.isEmpty()) {
            city = "Phnom Penh";
        }
        if (city.length() > 15) {
            city = city.substring(0, 15).trim();
        }
        appendTlv(tlv, "60", city);

        // Tag 62: Additional Data Field Template (Bill number alphanumeric only)
        StringBuilder additionalData = new StringBuilder();
        if (payload.getBillNumber() != null && !payload.getBillNumber().isBlank()) {
            String cleanBill = payload.getBillNumber().replaceAll("[^a-zA-Z0-9]", "");
            appendTlv(additionalData, "01", cleanBill);
        }
        if (!additionalData.isEmpty()) {
            appendTlv(tlv, "62", additionalData.toString());
        }

        // Tag 99: Bakong Timestamp & Expiration (Official format for Dynamic QR)
        long nowTimestamp = System.currentTimeMillis();
        long expiryTimestamp = nowTimestamp + (30L * 24 * 3600 * 1000L);
        StringBuilder timestampData = new StringBuilder();
        appendTlv(timestampData, "00", String.valueOf(nowTimestamp));
        appendTlv(timestampData, "01", String.valueOf(expiryTimestamp));
        appendTlv(tlv, "99", timestampData.toString());

        // Tag 63: Checksum prefix (6304)
        tlv.append("6304");

        // Compute 4-character CRC-16 CCITT
        String crc = KhqrCrc16.calculateCrc(tlv.toString());
        String finalKhqrString = tlv.toString() + crc;

        // Compute MD5 hash as hex (used by Bakong Open API)
        String md5 = DigestUtils.md5DigestAsHex(finalKhqrString.getBytes(StandardCharsets.UTF_8));

        return new KhqrData(finalKhqrString, md5);
    }

    private void appendTlv(StringBuilder sb, String tag, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        sb.append(tag)
          .append(String.format("%02d", bytes.length))
          .append(value);
    }
}

