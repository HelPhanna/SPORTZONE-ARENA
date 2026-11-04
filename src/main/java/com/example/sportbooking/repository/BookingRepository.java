package com.example.sportbooking.repository;

import com.example.sportbooking.entity.Booking;
import com.example.sportbooking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime dateTime);

    List<Booking> findByStatusAndBookingDateBefore(BookingStatus status, LocalDate date);

    long countByUserIdAndStatusInAndBookingDateGreaterThanEqual(
            Long userId,
            List<BookingStatus> statuses,
            LocalDate date
    );

    Page<Booking> findByUserIdOrderByBookingDateDescStartTimeDesc(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndBookingDateGreaterThanEqualAndStatusInOrderByBookingDateAscStartTimeAsc(
            Long userId,
            LocalDate date,
            List<BookingStatus> statuses,
            Pageable pageable
    );

    Page<Booking> findByUserIdAndBookingDateLessThanOrStatusInOrderByBookingDateDescStartTimeDesc(
            Long userId,
            LocalDate date,
            List<BookingStatus> historyStatuses,
            Pageable pageable
    );

    boolean existsByFieldIdAndBookingDateGreaterThanEqualAndStatusIn(
            Long fieldId,
            LocalDate date,
            List<BookingStatus> statuses
    );

    boolean existsByUserId(Long userId);

    @Query("SELECT b FROM Booking b WHERE " +
            "(:fieldId IS NULL OR b.field.id = :fieldId) AND " +
            "(:categoryId IS NULL OR b.field.category.id = :categoryId) AND " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:startDate IS NULL OR b.bookingDate >= :startDate) AND " +
            "(:endDate IS NULL OR b.bookingDate <= :endDate) AND " +
            "(:keyword IS NULL OR LOWER(b.bookingCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(b.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            " LOWER(b.customerPhone) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Booking> searchAdminBookings(@Param("keyword") String keyword,
                                     @Param("fieldId") Long fieldId,
                                     @Param("categoryId") Long categoryId,
                                     @Param("status") BookingStatus status,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate,
                                     Pageable pageable);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingDate = :date AND b.status IN :statuses")
    long countBookingsOnDate(@Param("date") LocalDate date, @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingDate BETWEEN :start AND :end AND b.status IN :statuses")
    long countBookingsBetweenDates(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM Booking b WHERE b.bookingDate = :date AND b.status IN :statuses")
    BigDecimal sumRevenueOnDate(@Param("date") LocalDate date, @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM Booking b WHERE b.bookingDate BETWEEN :start AND :end AND b.status IN :statuses")
    BigDecimal sumRevenueBetweenDates(@Param("start") LocalDate start, @Param("end") LocalDate end, @Param("statuses") List<BookingStatus> statuses);

    @Query("SELECT COALESCE(SUM(b.paidAmount), 0) FROM Booking b WHERE b.status IN :statuses")
    BigDecimal sumTotalRevenue(@Param("statuses") List<BookingStatus> statuses);

    long countByStatus(BookingStatus status);

    List<Booking> findTop10ByOrderByCreatedAtDesc();

    List<Booking> findByFieldIdAndBookingDateOrderByStartTimeAsc(Long fieldId, LocalDate bookingDate);
}
