package com.example.sportbooking.controller.api;

import com.example.sportbooking.entity.Booking;
import com.example.sportbooking.entity.Payment;
import com.example.sportbooking.entity.PaymentStatus;
import com.example.sportbooking.repository.BookingRepository;
import com.example.sportbooking.repository.PaymentRepository;
import com.example.sportbooking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentApiController {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    /**
     * Polling endpoint used by the browser to detect payment completion in real time.
     */
    @GetMapping("/{bookingCode}/status")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode).orElse(null);
        if (booking == null) {
            return ResponseEntity.notFound().build();
        }

        List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
        Payment latestPayment = payments.isEmpty() ? null : payments.get(payments.size() - 1);

        // Try checking Bakong API if still pending
        if (latestPayment != null && latestPayment.getStatus() == PaymentStatus.PENDING) {
            paymentService.checkAndUpdateStatus(bookingCode);
            // Refresh payment from DB
            latestPayment = paymentRepository.findById(latestPayment.getId()).orElse(latestPayment);
            booking = bookingRepository.findById(booking.getId()).orElse(booking);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("bookingCode", booking.getBookingCode());
        response.put("bookingStatus", booking.getStatus().name());
        response.put("paymentStatus", latestPayment != null ? latestPayment.getStatus().name() : "NONE");
        response.put("isPaid", "PAID".equals(booking.getStatus().name()) || "COMPLETED".equals(booking.getStatus().name()));

        return ResponseEntity.ok(response);
    }

    /**
     * Developer convenience endpoint to trigger simulation via AJAX.
     */
    @PostMapping("/{bookingCode}/simulate")
    public ResponseEntity<Map<String, Object>> simulatePayment(@PathVariable String bookingCode) {
        Payment payment = paymentService.simulatePaymentSuccess(bookingCode);
        if (payment == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("bookingCode", bookingCode);
        response.put("paymentStatus", payment.getStatus().name());
        return ResponseEntity.ok(response);
    }
}

