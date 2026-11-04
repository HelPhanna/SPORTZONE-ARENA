package com.example.sportbooking;

import com.example.sportbooking.dto.DayScheduleDto;
import com.example.sportbooking.dto.SlotDto;
import com.example.sportbooking.entity.*;
import com.example.sportbooking.repository.BlockedSlotRepository;
import com.example.sportbooking.repository.BookingSlotRepository;
import com.example.sportbooking.service.impl.SlotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceImplTest {

    @Mock
    private BookingSlotRepository bookingSlotRepository;

    @Mock
    private BlockedSlotRepository blockedSlotRepository;

    @InjectMocks
    private SlotServiceImpl slotService;

    private Field testField;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .id(1L)
                .name("Badminton")
                .status(CategoryStatus.ACTIVE)
                .build();

        testField = Field.builder()
                .id(5L)
                .name("Court 1")
                .category(category)
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(12, 0))
                .slotMinutes(60)
                .pricePerSlot(BigDecimal.valueOf(20))
                .peakPrice(BigDecimal.valueOf(30))
                .peakStartTime(LocalTime.of(10, 0))
                .status(FieldStatus.ACTIVE)
                .build();
    }

    @Test
    void getSlotsForFieldAndDate_FutureDate_AllSlotsGenerated() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        when(bookingSlotRepository.findByFieldIdAndBookingDate(eq(5L), eq(tomorrow)))
                .thenReturn(Collections.emptyList());
        when(blockedSlotRepository.findByFieldIdAndBlockDate(eq(5L), eq(tomorrow)))
                .thenReturn(Collections.emptyList());

        List<SlotDto> slots = slotService.getSlotsForFieldAndDate(testField, tomorrow);

        // 8:00 - 12:00 with 60 min slots -> 4 slots
        assertEquals(4, slots.size());
        assertTrue(slots.stream().allMatch(SlotDto::isAvailable));
        assertEquals("08:00 - 09:00", slots.get(0).getFormattedTime());
        assertEquals("AVAILABLE", slots.get(0).getStatus());
        assertFalse(slots.get(0).isPeak());
        assertEquals(BigDecimal.valueOf(20), slots.get(0).getPrice());

        // Peak starts at 10:00 -> slots from 10:00 should be peak
        assertTrue(slots.get(2).isPeak());
        assertEquals(BigDecimal.valueOf(30), slots.get(2).getPrice());
    }

    @Test
    void getNext7DaysSchedule_Returns7DaysWithCorrectLabels() {
        when(bookingSlotRepository.findByFieldIdAndBookingDate(eq(5L), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(blockedSlotRepository.findByFieldIdAndBlockDate(eq(5L), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        LocalDate selectedDate = LocalDate.now().plusDays(2);
        List<DayScheduleDto> schedule = slotService.getNext7DaysSchedule(testField, selectedDate);

        assertNotNull(schedule);
        assertEquals(7, schedule.size());

        // First day is Today
        DayScheduleDto day0 = schedule.get(0);
        assertEquals("Today", day0.getDayName());
        assertTrue(day0.isToday());
        assertEquals(LocalDate.now(), day0.getDate());

        // Second day is Tomorrow
        DayScheduleDto day1 = schedule.get(1);
        assertEquals("Tomorrow", day1.getDayName());
        assertFalse(day1.isToday());
        assertEquals(LocalDate.now().plusDays(1), day1.getDate());

        // Third day should be selected
        DayScheduleDto day2 = schedule.get(2);
        assertTrue(day2.isSelected());
        assertEquals(selectedDate, day2.getDate());

        // Total slots should be 4
        assertEquals(4, day1.getTotalSlots());
    }

    @Test
    void getSlotsForNext7Days_ReturnsMapWith7Entries() {
        when(bookingSlotRepository.findByFieldIdAndBookingDate(eq(5L), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(blockedSlotRepository.findByFieldIdAndBlockDate(eq(5L), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        Map<LocalDate, List<SlotDto>> map = slotService.getSlotsForNext7Days(testField);

        assertNotNull(map);
        assertEquals(7, map.size());
        assertTrue(map.containsKey(LocalDate.now()));
        assertTrue(map.containsKey(LocalDate.now().plusDays(6)));
    }

    @Test
    void slotDtoSerialization_IncludesBothAvailableAndIsAvailable() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        SlotDto slot = SlotDto.builder()
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .formattedTime("09:00 - 10:00")
                .price(BigDecimal.valueOf(25))
                .isAvailable(true)
                .isPeak(false)
                .status("AVAILABLE")
                .build();

        String json = mapper.writeValueAsString(slot);
        assertTrue(json.contains("\"available\":true"));
        assertTrue(json.contains("\"isAvailable\":true"));
        assertTrue(json.contains("\"status\":\"AVAILABLE\""));
    }
}

