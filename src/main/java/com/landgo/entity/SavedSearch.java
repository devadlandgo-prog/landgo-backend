package com.landgo.entity;

import com.landgo.enums.ProjectStage;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "saved_searches")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SavedSearch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // --- Search Criteria ---

    @Column(name = "keyword", length = 200)
    private String keyword;

    @Column(name = "city", length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_stage", length = 30)
    private ProjectStage projectStage;

    @Column(name = "min_price", precision = 15, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 15, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "min_lot_size", precision = 15, scale = 2)
    private BigDecimal minLotSize;

    @Column(name = "max_lot_size", precision = 15, scale = 2)
    private BigDecimal maxLotSize;

    // --- Notification Settings ---

    @Column(name = "notifications_enabled")
    @Builder.Default
    private boolean notificationsEnabled = true;

    @Column(name = "match_count")
    @Builder.Default
    private int matchCount = 0;

    @Column(name = "last_notified_at")
    private java.time.LocalDateTime lastNotifiedAt;
}
