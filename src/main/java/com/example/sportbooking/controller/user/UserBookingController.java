package com.example.sportbooking.controller.user;

import com.example.sportbooking.config.CustomUserDetails;
import com.example.sportbooking.config.KhqrProperties;
import com.example.sportbooking.entity.*;
import com.example.sportbooking.repository.*;
import com.example.sportbooking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class UserBookingController {

    private final BookingRepository bookingRepository;
    private final BookingSlotRepository bookingSlotRepository;
    private final FieldRepository fieldRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final KhqrProperties khqrProperties;

    @PostMapping("/fields/{id}/book")
    @Transactional
    public String createBooking(@PathVariable("id") Long fieldId,
                                @RequestParam("bookingDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bookingDate,
                                @RequestParam("slots") List<String> selectedSlotStrings,
                                @RequestParam(value = "customerName", required = false) String customerName,
                                @RequestParam(value = "customerPhone", required = false) String customerPhone,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        Field field = fieldRepository.findById(fieldId).orElse(null);
        if (field == null || field.getStatus() != FieldStatus.ACTIVE) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected sport field is not available.");
            return "redirect:/fields";
        }

        if (selectedSlotStrings == null || selectedSlotStrings.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one time slot.");
            return "redirect:/fields/" + fieldId + "?date=" + bookingDate;
        }

        LocalDate today = LocalDate.now();
        LocalDate maxBookingDate = today.plusDays(7);
        if (bookingDate.isBefore(today)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot book for dates in the past.");
            return "redirect:/fields/" + fieldId;
        }
        if (bookingDate.isAfter(maxBookingDate)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Bookings can only be made for the next 7 days (up to " + maxBookingDate + ").");
            return "redirect:/fields/" + fieldId;
        }

        User user = null;
        if (userDetails != null) {
            user = userRepository.findById(userDetails.getId()).orElse(null);
        }

        String finalName = (customerName != null && !customerName.isBlank())
                ? customerName.trim()
                : (user != null ? user.getName() : "Guest Customer");

        String finalPhone = (customerPhone != null && !customerPhone.isBlank())
                ? customerPhone.trim()
                : (user != null && user.getPhone() != null ? user.getPhone() : "+85512345678");

        // Parse requested slots
        List<LocalTime> startTimes = new ArrayList<>();
        List<LocalTime> endTimes = new ArrayList<>();
        for (String slotStr : selectedSlotStrings) {
            String[] parts = slotStr.split("-");
            if (parts.length == 2) {
                startTimes.add(LocalTime.parse(parts[0].trim()));
                endTimes.add(LocalTime.parse(parts[1].trim()));
            }
        }
        Collections.sort(startTimes);
        Collections.sort(endTimes);

        LocalTime earliestStart = startTimes.get(0);
        LocalTime latestEnd = endTimes.get(endTimes.size() - 1);

        // Check if any slot is already booked (prevent race conditions)
        List<BookingSlot> existingBookings = bookingSlotRepository.findByFieldIdAndBookingDate(fieldId, bookingDate);
        Set<LocalTime> bookedTimes = new HashSet<>();
        for (BookingSlot bs : existingBookings) {
            if (bs.getBooking() != null &&
                    bs.getBooking().getStatus() != BookingStatus.CANCELLED &&
                    bs.getBooking().getStatus() != BookingStatus.EXPIRED) {
                bookedTimes.add(bs.getStartTime());
            }
        }

        if (bookingDate.isEqual(today)) {
            LocalTime now = LocalTime.now();
            for (LocalTime st : startTimes) {
                if (st.isBefore(now)) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "One of your selected time slots (" + st + ") has already passed today. Please pick an upcoming slot.");
                    return "redirect:/fields/" + fieldId + "?date=" + bookingDate;
                }
            }
        }

        for (LocalTime st : startTimes) {
            if (bookedTimes.contains(st)) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "One of your selected time slots (" + st + ") has just been reserved. Please pick another slot.");
                return "redirect:/fields/" + fieldId + "?date=" + bookingDate;
            }
        }

        // Calculate total price with peak pricing check
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (LocalTime st : startTimes) {
            BigDecimal slotPrice = field.getPricePerSlot();
            if (field.getPeakStartTime() != null && field.getPeakPrice() != null) {
                if (!st.isBefore(field.getPeakStartTime())) {
                    slotPrice = field.getPeakPrice();
                }
            }
            totalPrice = totalPrice.add(slotPrice);
        }

        // Generate unique booking code
        String bookingCode = "SB-" + LocalDate.now().toString().replace("-", "") + "-"
                + String.format("%05d", new Random().nextInt(100000));

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .user(user)
                .field(field)
                .customerName(finalName)
                .customerPhone(finalPhone)
                .bookingDate(bookingDate)
                .startTime(earliestStart)
                .endTime(latestEnd)
                .totalPrice(totalPrice)
                .paidAmount(BigDecimal.ZERO)
                .status(BookingStatus.PENDING)
                .source(BookingSource.ONLINE)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        booking = bookingRepository.save(booking);

        // Save individual booking slots
        for (int i = 0; i < startTimes.size(); i++) {
            BookingSlot bs = BookingSlot.builder()
                    .booking(booking)
                    .field(field)
                    .bookingDate(bookingDate)
                    .startTime(startTimes.get(i))
                    .endTime(endTimes.get(i))
                    .build();
            bookingSlotRepository.save(bs);
        }

        // Create Payment record with Bakong KHQR
        paymentService.createKhqrPayment(booking);

        redirectAttributes.addFlashAttribute("successMessage", "Booking created successfully! Please complete your KHQR payment within 15 minutes.");
        return "redirect:/bookings/" + bookingCode;
    }

    @GetMapping("/bookings/{bookingCode}")
    public String viewBookingDetail(@PathVariable("bookingCode") String bookingCode, Model model) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode).orElse(null);
        if (booking == null) {
            return "redirect:/";
        }

        List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
        Payment latestPayment = payments.isEmpty() ? null : payments.get(payments.size() - 1);
        String qrImageBase64 = paymentService.generateQrImageBase64(latestPayment);

        model.addAttribute("booking", booking);
        model.addAttribute("payment", latestPayment);
        model.addAttribute("qrImageBase64", qrImageBase64);
        model.addAttribute("mockMode", khqrProperties.isMockMode());
        model.addAttribute("activeMenu", "bookings");

        return "user/bookings/detail";
    }

    @PostMapping("/bookings/{bookingCode}/simulate-pay")
    @Transactional
    public String simulatePayment(@PathVariable("bookingCode") String bookingCode,
                                  RedirectAttributes redirectAttributes) {

        paymentService.simulatePaymentSuccess(bookingCode);

        redirectAttributes.addFlashAttribute("successMessage", "Payment confirmed via Bakong KHQR! Your booking is secured.");
        return "redirect:/bookings/" + bookingCode;
    }

    @GetMapping("/my-bookings")
    public String myBookings(@RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> bookingsPage;

        if ("upcoming".equalsIgnoreCase(status)) {
            bookingsPage = bookingRepository.findByUserIdAndBookingDateGreaterThanEqualAndStatusInOrderByBookingDateAscStartTimeAsc(
                    userDetails.getId(), LocalDate.now(),
                    List.of(BookingStatus.PENDING, BookingStatus.DEPOSIT_PAID, BookingStatus.PAID),
                    pageable);
        } else if ("history".equalsIgnoreCase(status)) {
            bookingsPage = bookingRepository.findByUserIdAndBookingDateLessThanOrStatusInOrderByBookingDateDescStartTimeDesc(
                    userDetails.getId(), LocalDate.now(),
                    List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED, BookingStatus.EXPIRED),
                    pageable);
        } else {
            bookingsPage = bookingRepository.findByUserIdOrderByBookingDateDescStartTimeDesc(userDetails.getId(), pageable);
        }

        model.addAttribute("bookingsPage", bookingsPage);
        model.addAttribute("currentStatusFilter", status);
        model.addAttribute("activeMenu", "my-bookings");

        return "user/bookings/my-bookings";
    }

    @PostMapping("/my-bookings/{id}/cancel")
    @Transactional
    public String cancelBooking(@PathVariable("id") Long id,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null || !booking.getUser().getId().equals(userDetails.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found or permission denied.");
            return "redirect:/my-bookings";
        }

        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.CANCELLED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot cancel an already completed or cancelled booking.");
            return "redirect:/my-bookings";
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        redirectAttributes.addFlashAttribute("successMessage", "Booking " + booking.getBookingCode() + " has been cancelled.");
        return "redirect:/my-bookings";
    }
}
