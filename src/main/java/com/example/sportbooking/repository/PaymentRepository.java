package com.example.sportbooking.repository;

import com.example.sportbooking.entity.Payment;
import com.example.sportbooking.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByMd5(String md5);

    List<Payment> findByBookingId(Long bookingId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:keyword IS NULL OR LOWER(p.booking.bookingCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(p.transactionRef) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(p.booking.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Payment> searchPayments(@Param("keyword") String keyword,
                                 @Param("status") PaymentStatus status,
                                 Pageable pageable);
}
