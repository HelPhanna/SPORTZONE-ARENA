package com.example.sportbooking;

import com.example.sportbooking.controller.admin.AdminBookingController;
import com.example.sportbooking.entity.*;
import com.example.sportbooking.repository.*;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminBookingControllerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingSlotRepository bookingSlotRepository;

    @Mock
    private FieldRepository fieldRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private AdminBookingController adminBookingController;

    private Field testField;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .id(1L)
                .name("Football")
                .status(CategoryStatus.ACTIVE)
                .build();

        testField = Field.builder()
                .id(10L)
                .name("Arena Field A")
                .category(category)
                .pricePerSlot(BigDecimal.valueOf(30))
                .peakPrice(BigDecimal.valueOf(40))
                .peakStartTime(LocalTime.of(17, 0))
                .status(FieldStatus.ACTIVE)
                .openTime(LocalTime.of(6, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();

        testBooking = Booking.builder()
                .id(100L)
                .bookingCode("SB-20261023-12345")
                .field(testField)
                .customerName("John Doe")
                .customerPhone("+85512345678")
                .bookingDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 0))
                .totalPrice(BigDecimal.valueOf(30))
                .paidAmount(BigDecimal.valueOf(30))
                .status(BookingStatus.PAID)
                .source(BookingSource.WALK_IN)
                .build();
    }

    @Test
    void listBookings_ReturnsListView() {
        Page<Booking> page = new PageImpl<>(List.of(testBooking));
        when(bookingRepository.searchAdminBookings(any(), any(), any(), any(), any(), any(), any(PageRequest.class)))
                .thenReturn(page);
        when(bookingRepository.count()).thenReturn(1L);
        when(bookingRepository.countByStatus(any(BookingStatus.class))).thenReturn(0L);

        String view = adminBookingController.listBookings(
                null, null, null, null, null, null, 0, 15, model);

        assertEquals("admin/bookings/list", view);
        verify(model).addAttribute(eq("bookingsPage"), eq(page));
        verify(model).addAttribute(eq("activeMenu"), eq("bookings"));
    }

    @Test
    void showCreateForm_ReturnsCreateView() {
        when(fieldRepository.findByStatus(FieldStatus.ACTIVE)).thenReturn(List.of(testField));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        String view = adminBookingController.showCreateForm(model);

        assertEquals("admin/bookings/create", view);
        verify(model).addAttribute(eq("fields"), eq(List.of(testField)));
        verify(model).addAttribute(eq("activeMenu"), eq("bookings"));
    }

    @Test
    void createBooking_Success_RedirectsToDetail() {
        when(fieldRepository.findById(10L)).thenReturn(Optional.of(testField));
        when(bookingSlotRepository.findByFieldIdAndBookingDate(eq(10L), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        String view = adminBookingController.createBooking(
                10L,
                LocalDate.now().plusDays(1),
                List.of("08:00-09:00"),
                "Sokha Chan",
                "+85512345678",
                null,
                BookingStatus.PAID,
                BookingSource.WALK_IN,
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                PaymentMethod.CASH,
                redirectAttributes
        );

        assertEquals("redirect:/admin/bookings/100", view);
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(bookingSlotRepository, times(1)).save(any(BookingSlot.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void viewBookingDetail_Success_ReturnsDetailView() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findByBookingId(100L)).thenReturn(Collections.emptyList());

        String view = adminBookingController.viewBookingDetail(100L, model, redirectAttributes);

        assertEquals("admin/bookings/detail", view);
        verify(model).addAttribute(eq("booking"), eq(testBooking));
        verify(model).addAttribute(eq("activeMenu"), eq("bookings"));
    }

    @Test
    void showEditForm_Success_ReturnsEditView() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(testBooking));
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(fieldRepository.findAll()).thenReturn(List.of(testField));

        String view = adminBookingController.showEditForm(100L, model, redirectAttributes);

        assertEquals("admin/bookings/edit", view);
        verify(model).addAttribute(eq("booking"), eq(testBooking));
    }

    @Test
    void updateBooking_Success_RedirectsToDetail() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        String view = adminBookingController.updateBooking(
                100L,
                "Jane Doe",
                "+85598765432",
                null,
                BookingStatus.COMPLETED,
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(30),
                BookingSource.WALK_IN,
                redirectAttributes
        );

        assertEquals("redirect:/admin/bookings/100", view);
        assertEquals("Jane Doe", testBooking.getCustomerName());
        assertEquals("+85598765432", testBooking.getCustomerPhone());
        assertEquals(BookingStatus.COMPLETED, testBooking.getStatus());
        verify(bookingRepository).save(testBooking);
    }

    @Test
    void updateBookingStatus_ToCancelled_DeletesSlots() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(testBooking);

        String view = adminBookingController.updateBookingStatus(
                100L,
                BookingStatus.CANCELLED,
                PaymentMethod.CASH,
                redirectAttributes
        );

        assertEquals("redirect:/admin/bookings/100", view);
        assertEquals(BookingStatus.CANCELLED, testBooking.getStatus());
        verify(bookingSlotRepository).deleteByBookingId(100L);
    }

    @Test
    void deleteBooking_Success_RedirectsToList() {
        when(bookingRepository.findById(100L)).thenReturn(Optional.of(testBooking));

        String view = adminBookingController.deleteBooking(100L, redirectAttributes);

        assertEquals("redirect:/admin/bookings", view);
        verify(bookingRepository).delete(testBooking);
    }
}
