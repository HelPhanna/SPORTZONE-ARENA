package com.example.sportbooking.service;

import com.example.sportbooking.dto.DayScheduleDto;
import com.example.sportbooking.dto.SlotDto;
import com.example.sportbooking.entity.Field;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface SlotService {

    /**
     * Retrieve generated slots and availability for a specific field on a single date.
     */
    List<SlotDto> getSlotsForFieldAndDate(Field field, LocalDate date);

    /**
     * Retrieve the full 7-day schedule summary and slots starting from today.
     */
    List<DayScheduleDto> getNext7DaysSchedule(Field field);

    /**
     * Retrieve the full 7-day schedule summary and slots starting from today,
     * highlighting the currently selected date.
     */
    List<DayScheduleDto> getNext7DaysSchedule(Field field, LocalDate selectedDate);

    /**
     * Retrieve slots grouped by date for the next 7 days starting from today.
     */
    Map<LocalDate, List<SlotDto>> getSlotsForNext7Days(Field field);

    /**
     * Retrieve slots grouped by date for 7 consecutive days starting from the given startDate.
     */
    Map<LocalDate, List<SlotDto>> getSlotsForNext7Days(Field field, LocalDate startDate);
}
