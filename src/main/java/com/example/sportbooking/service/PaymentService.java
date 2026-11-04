package com.example.sportbooking.service;

import com.example.sportbooking.entity.Booking;
import com.example.sportbooking.entity.Payment;

public interface PaymentService {

    /**
     * Creates a new KHQR payment record for a given booking.
     */
    Payment createKhqrPayment(Booking booking);

    /**
     * Generates a Base64 data URI image for the payment's KHQR string.
     */
    String generateQrImageBase64(Payment payment);

    /**
     * Checks payment status with Bakong API and updates booking and payment if paid.
     *
     * @param bookingCode the booking code
     * @return true if paid/success, false otherwise
     */
    boolean checkAndUpdateStatus(String bookingCode);

    /**
     * Simulates payment success for testing / demonstration purposes.
     *
     * @param bookingCode the booking code
     * @return the updated Payment entity
     */
    Payment simulatePaymentSuccess(String bookingCode);
}

