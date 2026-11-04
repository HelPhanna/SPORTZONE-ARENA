package com.example.sportbooking.khqr;

import com.example.sportbooking.config.KhqrProperties;
import com.example.sportbooking.khqr.dto.KhqrData;
import com.example.sportbooking.khqr.dto.KhqrPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class KhqrGeneratorTest {

    private KhqrGenerator khqrGenerator;
    private KhqrProperties properties;
    private QrCodeRenderService qrCodeRenderService;

    @BeforeEach
    void setUp() {
        properties = new KhqrProperties();
        properties.setAccountId("sportzone@aba");
        properties.setMerchantName("SPORTZONE ARENA");
        properties.setMerchantCity("Phnom Penh");
        properties.setMcc("5999");
        properties.setCurrency("USD");
        properties.setAcquiringBank("");

        khqrGenerator = new KhqrGenerator(properties);
        qrCodeRenderService = new QrCodeRenderService();
    }

    @Test
    @DisplayName("Should generate valid EMVCo KHQR string with MD5 and CRC-16")
    void testGenerateDynamicKhqr() {
        KhqrPayload payload = KhqrPayload.builder()
                .amount(new BigDecimal("25.00"))
                .currency("USD")
                .billNumber("SB-20261103-001")
                .build();

        KhqrData data = khqrGenerator.generateDynamicKhqr(payload);

        assertNotNull(data);
        assertNotNull(data.getKhqrString());
        assertNotNull(data.getMd5());

        String qr = data.getKhqrString();

        // Must start with Payload Format Indicator "000201"
        assertTrue(qr.startsWith("000201"));

        // Must contain Dynamic indicator "010212"
        assertTrue(qr.contains("010212"));

        // Must contain Tag 54 with amount 25.00
        assertTrue(qr.contains("540525.00"));

        // Must contain Tag 53 with USD currency code 840
        assertTrue(qr.contains("5303840"));

        // Must contain CRC Tag 6304 followed by 4 hex characters at the end
        assertTrue(qr.contains("6304"));
        assertEquals(4, qr.substring(qr.lastIndexOf("6304") + 4).length());

        // MD5 must be a 32-character hexadecimal string
        assertEquals(32, data.getMd5().length());
        assertTrue(data.getMd5().matches("^[a-f0-9]{32}$"));

        // Test ZXing QR generation
        String base64Image = qrCodeRenderService.generateBase64Qr(qr, 200, 200);
        assertNotNull(base64Image);
        assertTrue(base64Image.startsWith("data:image/png;base64,"));
    }
}

