package com.example.sportbooking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "field_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @Column(name = "image_path", nullable = false, length = 255)
    private String imagePath;

    @Column(name = "is_main", nullable = false)
    @Builder.Default
    private boolean isMain = false;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}
