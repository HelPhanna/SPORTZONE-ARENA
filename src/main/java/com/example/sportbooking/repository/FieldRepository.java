package com.example.sportbooking.repository;

import com.example.sportbooking.entity.Field;
import com.example.sportbooking.entity.FieldStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {

    List<Field> findByStatus(FieldStatus status);

    List<Field> findByCategoryIdAndStatus(Long categoryId, FieldStatus status);

    long countByCategoryId(Long categoryId);

    @Query("SELECT f FROM Field f WHERE " +
            "(:keyword IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR f.category.id = :categoryId) " +
            "AND (:status IS NULL OR f.status = :status)")
    Page<Field> searchAdminFields(@Param("keyword") String keyword,
                                 @Param("categoryId") Long categoryId,
                                 @Param("status") FieldStatus status,
                                 Pageable pageable);

    @Query("SELECT f FROM Field f WHERE f.status = 'ACTIVE' " +
            "AND (:categoryId IS NULL OR f.category.id = :categoryId) " +
            "AND (:keyword IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(f.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:minPrice IS NULL OR f.pricePerSlot >= :minPrice) " +
            "AND (:maxPrice IS NULL OR f.pricePerSlot <= :maxPrice)")
    Page<Field> searchActiveFields(@Param("categoryId") Long categoryId,
                                  @Param("keyword") String keyword,
                                  @Param("minPrice") BigDecimal minPrice,
                                  @Param("maxPrice") BigDecimal maxPrice,
                                  Pageable pageable);
}
