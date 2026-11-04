package com.example.sportbooking.service.impl;

import com.example.sportbooking.dto.DayScheduleDto;
import com.example.sportbooking.dto.SlotDto;
import com.example.sportbooking.entity.BlockedSlot;
import com.example.sportbooking.entity.BookingSlot;
import com.example.sportbooking.entity.BookingStatus;
import com.example.sportbooking.entity.Field;
import com.example.sportbooking.repository.BlockedSlotRepository;
import com.example.sportbooking.repository.BookingSlotRepository;
import com.example.sportbooking.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {

    private final BookingSlotRepository bookingSlotRepository;
    private final BlockedSlotRepository blockedSlotRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DAY_NAME_FMT = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_NUM_FMT = DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH);

    @Override
    @Transactional(readOnly = true)
    public List<SlotDto> getSlotsForFieldAndDate(Field field, LocalDate date) {
        List<SlotDto> slots = new ArrayList<>();
        if (field == null || date == null) {
            return slots;
        }

        LocalTime openTime = field.getOpenTime();
        LocalTime closeTime = field.getCloseTime();
        int slotMinutes = (field.getSlotMinutes() != null && field.getSlotMinutes() > 0)
                ? field.getSlotMinutes() : 60;

        // Fetch existing bookings for this field and date (excluding cancelled or expired)
        List<BookingSlot> bookedSlots = bookingSlotRepository.findByFieldIdAndBookingDate(field.getId(), date);
        Set<LocalTime> bookedStartTimes = bookedSlots.stream()
                .filter(bs -> bs.getBooking() != null &&
                        bs.getBooking().getStatus() != BookingStatus.CANCELLED &&
                        bs.getBooking().getStatus() != BookingStatus.EXPIRED)
                .map(BookingSlot::getStartTime)
                .collect(Collectors.toSet());

        // Fetch blocked slots
        List<BlockedSlot> blockedSlots = blockedSlotRepository.findByFieldIdAndBlockDate(field.getId(), date);

        LocalTime now = LocalTime.now();
        boolean isToday = date.isEqual(LocalDate.now());
        boolean isPastDate = date.isBefore(LocalDate.now());

        LocalTime currentSlotStart = openTime;
        while (currentSlotStart.plusMinutes(slotMinutes).compareTo(closeTime) <= 0
                || (closeTime.equals(LocalTime.MIDNIGHT) && !currentSlotStart.equals(LocalTime.MIDNIGHT))) {

            LocalTime currentSlotEnd = currentSlotStart.plusMinutes(slotMinutes);

            // Determine if peak pricing applies
            boolean isPeak = false;
            BigDecimal price = field.getPricePerSlot();
            if (field.getPeakStartTime() != null && field.getPeakPrice() != null) {
                if (!currentSlotStart.isBefore(field.getPeakStartTime())) {
                    isPeak = true;
                    price = field.getPeakPrice();
                }
            }

            // Check if slot is booked
            boolean isBooked = bookedStartTimes.contains(currentSlotStart);

            // Check if slot is blocked by admin
            LocalTime finalStart = currentSlotStart;
            boolean isBlocked = blockedSlots.stream().anyMatch(b ->
                    !finalStart.isBefore(b.getStartTime()) && finalStart.isBefore(b.getEndTime()));

            // Check if time has already passed
            boolean isPast = isPastDate || (isToday && currentSlotStart.isBefore(now));

            String status = "AVAILABLE";
            boolean isAvailable = true;

            if (isBooked) {
                status = "BOOKED";
                isAvailable = false;
            } else if (isBlocked) {
                status = "BLOCKED";
                isAvailable = false;
            } else if (isPast) {
                status = "PAST";
                isAvailable = false;
            }

            slots.add(SlotDto.builder()
                    .startTime(currentSlotStart)
                    .endTime(currentSlotEnd)
                    .formattedTime(currentSlotStart.format(TIME_FMT) + " - " + currentSlotEnd.format(TIME_FMT))
                    .price(price)
                    .isAvailable(isAvailable)
                    .isPeak(isPeak)
                    .status(status)
                    .build());

            currentSlotStart = currentSlotEnd;
            if (currentSlotStart.equals(closeTime) || currentSlotStart.equals(LocalTime.MIDNIGHT)) {
                break;
            }
        }

        return slots;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DayScheduleDto> getNext7DaysSchedule(Field field) {
        return getNext7DaysSchedule(field, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DayScheduleDto> getNext7DaysSchedule(Field field, LocalDate selectedDate) {
        if (field == null) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();
        LocalDate activeDate = (selectedDate != null && !selectedDate.isBefore(today)) ? selectedDate : today;
        List<DayScheduleDto> days = new ArrayList<>(7);

        for (int i = 0; i < 7; i++) {
            LocalDate date = today.plusDays(i);
            List<SlotDto> slots = getSlotsForFieldAndDate(field, date);
            long availableCount = slots.stream().filter(SlotDto::isAvailable).count();

            String dayName;
            if (i == 0) {
                dayName = "Today";
            } else if (i == 1) {
                dayName = "Tomorrow";
            } else {
                dayName = date.format(DAY_NAME_FMT);
            }

            days.add(DayScheduleDto.builder()
                    .date(date)
                    .dayName(dayName)
                    .dayOfWeek(date.format(DAY_NAME_FMT))
                    .formattedDate(date.format(DATE_NUM_FMT))
                    .isToday(i == 0)
                    .isSelected(date.isEqual(activeDate))
                    .totalSlots(slots.size())
                    .availableSlots((int) availableCount)
                    .slots(slots)
                    .build());
        }

        return days;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, List<SlotDto>> getSlotsForNext7Days(Field field) {
        return getSlotsForNext7Days(field, LocalDate.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, List<SlotDto>> getSlotsForNext7Days(Field field, LocalDate startDate) {
        if (field == null) {
            return Collections.emptyMap();
        }

        LocalDate start = (startDate != null && !startDate.isBefore(LocalDate.now())) ? startDate : LocalDate.now();
        Map<LocalDate, List<SlotDto>> schedule = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate d = start.plusDays(i);
            schedule.put(d, getSlotsForFieldAndDate(field, d));
        }

        return schedule;
    }
}
