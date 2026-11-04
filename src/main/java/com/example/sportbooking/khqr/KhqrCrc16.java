package com.example.sportbooking.khqr;

import java.nio.charset.StandardCharsets;

public final class KhqrCrc16 {

    private KhqrCrc16() {}

    /**
     * Calculates the CRC-16/CCITT-FALSE checksum required by EMVCo / KHQR.
     * Polynomial: 0x1021, Initial value: 0xFFFF
     *
     * @param data The string to checksum (including the "6304" tag & length)
     * @return 4-character uppercase hexadecimal string
     */
    public static String calculateCrc(String data) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }
}

