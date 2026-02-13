package com.landgo.dto.response;

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
public class VendorResponse {

    private UUID id;
    private UUID userId;
    private String companyName;
    private String companyDescription;
    private String companyLogo;
    private String businessAddress;
    private String businessCity;
    private String businessState;
    private String businessZipCode;
    private String businessCountry;
    private String website;
    private boolean verified;
    private BigDecimal rating;
    private Integer totalReviews;
    private Integer totalLandsListed;
    private Integer totalLandsSold;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdAt;
}
