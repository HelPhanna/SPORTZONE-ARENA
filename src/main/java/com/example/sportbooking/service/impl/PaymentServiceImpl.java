package com.example.sportbooking.service.impl;

import com.example.sportbooking.config.KhqrProperties;
import com.example.sportbooking.entity.Booking;
import com.example.sportbooking.entity.BookingStatus;
import com.example.sportbooking.entity.Payment;
import com.example.sportbooking.entity.PaymentMethod;
import com.example.sportbooking.entity.PaymentStatus;
import com.example.sportbooking.khqr.KhqrGenerator;
import com.example.sportbooking.khqr.QrCodeRenderService;
import com.example.sportbooking.khqr.client.BakongApiClient;
import com.example.sportbooking.khqr.dto.KhqrData;
import com.example.sportbooking.khqr.dto.KhqrPayload;
import com.example.sportbooking.repository.BookingRepository;
import com.example.sportbooking.repository.PaymentRepository;
import com.example.sportbooking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final KhqrGenerator khqrGenerator;
    private final QrCodeRenderService qrCodeRenderService;
    private final BakongApiClient bakongApiClient;
    private final KhqrProperties properties;

    @Override
    @Transactional
    public Payment createKhqrPayment(Booking booking) {
        KhqrPayload payload = KhqrPayload.builder()
                .amount(booking.getTotalPrice())
                .currency(properties.getCurrency())
                .billNumber(booking.getBookingCode())
                .build();

        KhqrData khqrData = khqrGenerator.generateDynamicKhqr(payload);

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalPrice())
                .method(PaymentMethod.KHQR)
                .status(PaymentStatus.PENDING)
                .khqrString(khqrData.getKhqrString())
                .md5(khqrData.getMd5())
                .transactionRef("TXN-" + booking.getBookingCode())
                .build();

        log.info("Created KHQR Payment for booking {}: MD5={}", booking.getBookingCode(), khqrData.getMd5());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public String generateQrImageBase64(Payment payment) {
        if (payment == null) {
            return null;
        }
        // If still pending, re-generate dynamic QR using latest properties so dev changes take effect on refresh
        if (payment.getStatus() == PaymentStatus.PENDING && payment.getBooking() != null) {
            KhqrPayload payload = KhqrPayload.builder()
                    .amount(payment.getAmount())
                    .currency(properties.getCurrency())
                    .billNumber(payment.getBooking().getBookingCode())
                    .build();
            KhqrData khqrData = khqrGenerator.generateDynamicKhqr(payload);
            payment.setKhqrString(khqrData.getKhqrString());
            payment.setMd5(khqrData.getMd5());
            paymentRepository.save(payment);
        }
        return qrCodeRenderService.generateBase64Qr(payment.getKhqrString(), 300, 300);
    }

    @Override
    @Transactional
    public boolean checkAndUpdateStatus(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode).orElse(null);
        if (booking == null) {
            return false;
        }

        List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
        if (payments.isEmpty()) {
            return false;
        }

        Payment payment = payments.get(payments.size() - 1);
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return true;
        }

        // Query Bakong Open API
        boolean isPaid = bakongApiClient.checkTransactionByMd5(payment.getMd5());
        if (isPaid) {
            markPaymentSuccess(booking, payment);
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public Payment simulatePaymentSuccess(String bookingCode) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode).orElse(null);
        if (booking == null) {
            return null;
        }

        List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
        Payment payment = payments.isEmpty() ? null : payments.get(payments.size() - 1);

        if (payment != null) {
            markPaymentSuccess(booking, payment);
        }

        return payment;
    }

    private void markPaymentSuccess(Booking booking, Payment payment) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.PAID);
        booking.setPaidAmount(booking.getTotalPrice());
        bookingRepository.save(booking);

        log.info("Booking {} confirmed paid via KHQR payment #{}", booking.getBookingCode(), payment.getId());
    }
}

