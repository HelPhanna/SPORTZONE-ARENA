package com.example.sportbooking.repository;

import com.example.sportbooking.entity.Category;
import com.example.sportbooking.entity.CategoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Category> findByStatusOrderByNameAsc(CategoryStatus status);

    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.fields WHERE c.status = :status ORDER BY c.name ASC")
    List<Category> findActiveCategoriesWithFields(@Param("status") CategoryStatus status);

    @Query("SELECT c FROM Category c WHERE " +
            "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Category> searchCategories(@Param("keyword") String keyword, Pageable pageable);
}
