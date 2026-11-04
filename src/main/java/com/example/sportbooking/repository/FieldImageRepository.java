package com.example.sportbooking.repository;

import com.example.sportbooking.entity.FieldImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FieldImageRepository extends JpaRepository<FieldImage, Long> {
    List<FieldImage> findByFieldIdOrderBySortOrderAscIdAsc(Long fieldId);
    void deleteByFieldId(Long fieldId);
}
