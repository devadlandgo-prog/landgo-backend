package com.landgo.dto.request;

import com.landgo.enums.ProjectStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedSearchRequest {

    @NotBlank(message = "Search name is required")
    @Size(min = 1, max = 100, message = "Search name must be between 1 and 100 characters")
    private String name;

    @Size(max = 200, message = "Keyword must be at most 200 characters")
    private String keyword;

    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    private ProjectStage projectStage;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private BigDecimal minLotSize;

    private BigDecimal maxLotSize;

    @Builder.Default
    private boolean notificationsEnabled = true;
}
