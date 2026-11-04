package com.example.sportbooking;

import com.example.sportbooking.controller.admin.AdminFieldController;
import com.example.sportbooking.entity.*;
import com.example.sportbooking.repository.BookingRepository;
import com.example.sportbooking.repository.CategoryRepository;
import com.example.sportbooking.repository.FieldImageRepository;
import com.example.sportbooking.repository.FieldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminFieldControllerTest {

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FieldImageRepository fieldImageRepository;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminFieldController adminFieldController;

    private Field testField;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Football")
                .status(CategoryStatus.ACTIVE)
                .build();

        testField = Field.builder()
                .id(10L)
                .name("Arena Field A")
                .category(testCategory)
                .pricePerSlot(BigDecimal.valueOf(35))
                .peakPrice(BigDecimal.valueOf(45))
                .slotMinutes(60)
                .openTime(LocalTime.of(6, 0))
                .closeTime(LocalTime.of(23, 0))
                .status(FieldStatus.ACTIVE)
                .build();
    }

    @Test
    void listFields_ReturnsListView() {
        Page<Field> page = new PageImpl<>(List.of(testField));
        when(fieldRepository.searchAdminFields(any(), any(), any(), any(PageRequest.class))).thenReturn(page);
        when(categoryRepository.findAll()).thenReturn(List.of(testCategory));

        String view = adminFieldController.listFields(null, null, null, 0, 15, model);

        assertEquals("admin/fields/list", view);
        verify(model).addAttribute(eq("fieldsPage"), eq(page));
        verify(model).addAttribute(eq("activeMenu"), eq("fields"));
    }

    @Test
    void createField_Success_RedirectsToList() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(fieldRepository.save(any(Field.class))).thenReturn(testField);

        String view = adminFieldController.createField(
                "New Futsal Arena",
                1L,
                "Covered pitch with lighting",
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(40),
                LocalTime.of(17, 0),
                60,
                LocalTime.of(6, 0),
                LocalTime.of(23, 0),
                0,
                FieldStatus.ACTIVE,
                "/img/fields/futsal-1.jpg",
                redirectAttributes
        );

        assertEquals("redirect:/admin/fields", view);
        verify(fieldRepository).save(any(Field.class));
        verify(fieldImageRepository).save(any(FieldImage.class));
    }

    @Test
    void updateField_Success_RedirectsToList() {
        when(fieldRepository.findById(10L)).thenReturn(Optional.of(testField));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(fieldImageRepository.findByFieldIdOrderBySortOrderAscIdAsc(10L)).thenReturn(Collections.emptyList());
        when(fieldRepository.save(any(Field.class))).thenReturn(testField);

        String view = adminFieldController.updateField(
                10L,
                "Updated Arena Field A",
                1L,
                "Updated description",
                BigDecimal.valueOf(38),
                BigDecimal.valueOf(48),
                LocalTime.of(17, 0),
                60,
                LocalTime.of(6, 0),
                LocalTime.of(23, 0),
                10,
                FieldStatus.ACTIVE,
                "/img/fields/football-updated.jpg",
                redirectAttributes
        );

        assertEquals("redirect:/admin/fields", view);
        assertEquals("Updated Arena Field A", testField.getName());
        verify(fieldRepository).save(testField);
    }

    @Test
    void toggleStatus_TogglesActiveToInactive() {
        when(fieldRepository.findById(10L)).thenReturn(Optional.of(testField));
        when(fieldRepository.save(any(Field.class))).thenReturn(testField);

        String view = adminFieldController.toggleStatus(10L, redirectAttributes);

        assertEquals("redirect:/admin/fields", view);
        assertEquals(FieldStatus.INACTIVE, testField.getStatus());
        verify(fieldRepository).save(testField);
    }

    @Test
    void deleteField_Success_WhenNoActiveBookings() {
        when(fieldRepository.findById(10L)).thenReturn(Optional.of(testField));
        when(bookingRepository.existsByFieldIdAndBookingDateGreaterThanEqualAndStatusIn(any(), any(), any()))
                .thenReturn(false);

        String view = adminFieldController.deleteField(10L, redirectAttributes);

        assertEquals("redirect:/admin/fields", view);
        verify(fieldRepository).delete(testField);
    }

    @Test
    void deleteField_Blocks_WhenActiveBookingsExist() {
        when(fieldRepository.findById(10L)).thenReturn(Optional.of(testField));
        when(bookingRepository.existsByFieldIdAndBookingDateGreaterThanEqualAndStatusIn(any(), any(), any()))
                .thenReturn(true);

        String view = adminFieldController.deleteField(10L, redirectAttributes);

        assertEquals("redirect:/admin/fields", view);
        verify(fieldRepository, never()).delete(testField);
    }
}
