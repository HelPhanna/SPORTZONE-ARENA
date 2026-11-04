package com.example.sportbooking.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_per_slot", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerSlot;

    @Column(name = "peak_price", precision = 10, scale = 2)
    private BigDecimal peakPrice;

    @Column(name = "peak_start_time")
    private LocalTime peakStartTime;

    @Column(name = "slot_minutes", nullable = false)
    @Builder.Default
    private Integer slotMinutes = 60;

    @Column(name = "open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "deposit_percent", nullable = false)
    @Builder.Default
    private Integer depositPercent = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FieldStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC, id ASC")
    @Builder.Default
    private List<FieldImage> images = new ArrayList<>();

    public String getMainImagePath() {
        if (images != null && !images.isEmpty()) {
            for (FieldImage img : images) {
                if (img.isMain()) {
                    return img.getImagePath();
                }
            }
            return images.get(0).getImagePath();
        }
        return null;
    }
}
