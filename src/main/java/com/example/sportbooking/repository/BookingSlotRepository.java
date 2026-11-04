package com.example.sportbooking.repository;

import com.example.sportbooking.entity.BookingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingSlotRepository extends JpaRepository<BookingSlot, Long> {

    List<BookingSlot> findByFieldIdAndBookingDate(Long fieldId, LocalDate bookingDate);

    List<BookingSlot> findByBookingId(Long bookingId);

    @Modifying
    @Query("DELETE FROM BookingSlot bs WHERE bs.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}
