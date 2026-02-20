package com.landgo.dto.response;

import com.landgo.enums.LandStatus;
import com.landgo.enums.ProjectStage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandResponse {

    private UUID id;
    private ProjectStage projectStage;
    private LandStatus status;

    // Project Details
    private String address;
    private String city;
    private String postalCode;
    private BigDecimal lotSize;
    private String lotUnit;
    private String frontage;
    private String depth;
    private String currentZoningCodes;
    private String pinNumber;
    private String officialPlanDesignation;
    private BigDecimal latitude;
    private BigDecimal longitude;

    // Project Specification (flexible JSON)
    private Map<String, Object> projectSpecification;

    // Pricing
    private BigDecimal askingPrice;
    private String currency;
    private String pricingDescription;

    // Media
    private List<Map<String, String>> photos;
    private List<Map<String, String>> documents;

    // Ownership
    private String ownershipVerification;

    // Metrics
    private Integer viewCount;
    private Integer inquiryCount;

    // Vendor info (summary)
    private UUID vendorId;
    private String vendorCompanyName;
    private boolean vendorVerified;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
