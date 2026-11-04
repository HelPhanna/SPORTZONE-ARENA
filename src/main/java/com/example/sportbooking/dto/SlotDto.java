package com.example.sportbooking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotDto {
    private LocalTime startTime;
    private LocalTime endTime;
    private String formattedTime;
    private BigDecimal price;

    @JsonProperty("isAvailable")
    private boolean isAvailable;

    @JsonProperty("isPeak")
    private boolean isPeak;

    private String status; // AVAILABLE, BOOKED, BLOCKED, PAST

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available;
    }

    @JsonProperty("available")
    public boolean getAvailable() {
        return isAvailable;
    }

    public boolean isPeak() {
        return isPeak;
    }

    public void setPeak(boolean peak) {
        this.isPeak = peak;
    }

    @JsonProperty("peak")
    public boolean getPeak() {
        return isPeak;
    }
}
