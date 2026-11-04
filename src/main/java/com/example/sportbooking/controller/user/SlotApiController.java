package com.example.sportbooking.controller.user;

import com.example.sportbooking.dto.DayScheduleDto;
import com.example.sportbooking.dto.SlotDto;
import com.example.sportbooking.entity.Field;
import com.example.sportbooking.repository.FieldRepository;
import com.example.sportbooking.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/slots")
@RequiredArgsConstructor
public class SlotApiController {

    private final FieldRepository fieldRepository;
    private final SlotService slotService;

    @GetMapping
    public ResponseEntity<List<SlotDto>> getSlots(
            @RequestParam("fieldId") Long fieldId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Field field = fieldRepository.findById(fieldId).orElse(null);
        if (field == null) {
            return ResponseEntity.notFound().build();
        }

        List<SlotDto> slots = slotService.getSlotsForFieldAndDate(field, date);
        return ResponseEntity.ok(slots);
    }

    @GetMapping("/next-7-days")
    public ResponseEntity<List<DayScheduleDto>> getNext7DaysSchedule(
            @RequestParam("fieldId") Long fieldId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Field field = fieldRepository.findById(fieldId).orElse(null);
        if (field == null) {
            return ResponseEntity.notFound().build();
        }

        List<DayScheduleDto> schedule = slotService.getNext7DaysSchedule(field, date);
        return ResponseEntity.ok(schedule);
    }
}
