package com.example.sportbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayScheduleDto {

    private LocalDate date;
    private String dayName;        // e.g. "Today", "Tomorrow", "Wed"
    private String dayOfWeek;      // e.g. "Mon", "Tue"
    private String formattedDate;  // e.g. "03 Nov"
    private boolean isToday;
    private boolean isSelected;
    private int totalSlots;
    private int availableSlots;
    private List<SlotDto> slots;
}

