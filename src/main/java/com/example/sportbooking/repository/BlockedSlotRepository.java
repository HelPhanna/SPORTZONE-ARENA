package com.example.sportbooking.repository;

import com.example.sportbooking.entity.BlockedSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, Long> {

    List<BlockedSlot> findByFieldIdAndBlockDate(Long fieldId, LocalDate blockDate);

    @Query("SELECT bs FROM BlockedSlot bs WHERE " +
            "(:fieldId IS NULL OR bs.field.id = :fieldId) AND " +
            "(:date IS NULL OR bs.blockDate = :date)")
    Page<BlockedSlot> searchBlockedSlots(@Param("fieldId") Long fieldId,
                                         @Param("date") LocalDate date,
                                         Pageable pageable);
}
