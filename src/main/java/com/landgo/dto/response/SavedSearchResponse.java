package com.landgo.dto.response;

import com.landgo.enums.ProjectStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedSearchResponse {

    private UUID id;
    private String name;

    // Search Criteria
    private String keyword;
    private String city;
    private ProjectStage projectStage;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minLotSize;
    private BigDecimal maxLotSize;

    // Notification settings
    private boolean notificationsEnabled;
    private int matchCount;
    private LocalDateTime lastNotifiedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
