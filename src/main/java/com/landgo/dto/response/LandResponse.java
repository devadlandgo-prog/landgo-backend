package com.landgo.dto.response;

import com.landgo.enums.LandStatus;
import com.landgo.enums.LandType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandResponse {

    private UUID id;
    private String title;
    private String description;
    private LandType landType;
    private LandStatus status;
    
    // Location
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    
    // Specifications
    private BigDecimal price;
    private BigDecimal areaSqFt;
    private BigDecimal frontage;
    private BigDecimal depth;
    
    // Features
    private boolean hasWaterAccess;
    private boolean hasElectricity;
    private boolean hasRoadAccess;
    private boolean hasSewage;
    private String zoningInfo;
    private String topography;
    private String soilType;
    
    // Media
    private List<String> imageUrls;
    private String videoUrl;
    private String virtualTourUrl;
    
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
