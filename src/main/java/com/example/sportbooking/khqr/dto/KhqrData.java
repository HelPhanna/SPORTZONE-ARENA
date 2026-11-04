package com.example.sportbooking.khqr.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhqrData {
    private String khqrString;
    private String md5;
    private String qrImageBase64;

    public KhqrData(String khqrString, String md5) {
        this.khqrString = khqrString;
        this.md5 = md5;
    }
}

