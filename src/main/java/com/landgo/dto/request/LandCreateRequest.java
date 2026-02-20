package com.landgo.dto.request;

import com.landgo.enums.ProjectStage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LandCreateRequest {

    @NotNull(message = "Project stage is required")
    private ProjectStage projectStage;

    @NotNull(message = "Project details are required")
    @Valid
    private ProjectDetailsDto projectDetails;

    @Valid
    private ProjectSpecificationDto projectSpecification;

    @NotNull(message = "Pricing is required")
    @Valid
    private PricingDto pricing;

    @Size(max = 10, message = "You can upload a maximum of 10 photos")
    private List<FileDto> photos;

    @Size(max = 25, message = "You can upload a maximum of 25 documents")
    private List<FileDto> documents;

    private String ownershipVerification;

    // ===== Nested DTOs =====

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectDetailsDto {

        @NotBlank(message = "Address is required")
        private String address;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "Postal code is required")
        private String postalCode;

        @NotNull(message = "Lot size is required")
        @Positive(message = "Lot size must be positive")
        private BigDecimal lotSize;

        @NotBlank(message = "Lot unit is required")
        private String lotUnit;

        private String frontage;
        private String depth;
        private String currentZoningCodes;
        private String pinNumber;
        private String officialPlanDesignation;

        @Valid
        private CoordinatesDto coordinates;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinatesDto {
        private BigDecimal lat;
        private BigDecimal lng;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectSpecificationDto {
        private String buildingType;
        private String proposedUse;

        // Site Plan Approval
        private String sitePlanStatus;

        // Draft Plan Approval
        private String subdivisionType;
        private String lotBlockType;
        private String draftPlanStatus;

        @Valid
        private ServicesDto services;

        // Under City Submission
        @Valid
        private ProposedDevelopmentTypeDto proposedDevelopmentType;
        private String submissionStatus;

        // Ready-to-Shovel
        private String projectType;
        private String sellingType;
        private String constructionStartTimeline;
        private String approvalStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServicesDto {
        private Boolean gas;
        private Boolean hydro;
        private Boolean municipalSewer;
        private Boolean municipalWater;
        private Boolean septic;
        private Boolean well;
        private Boolean none;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposedDevelopmentTypeDto {
        private Boolean rezoning;
        private Boolean sitePlan;
        private Boolean subdivision;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricingDto {
        @NotNull(message = "Asking price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Asking price must be greater than 0")
        private BigDecimal askingPrice;

        @NotBlank(message = "Currency is required")
        private String currency;

        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileDto {
        @NotBlank(message = "File name is required")
        private String name;

        @NotBlank(message = "File type is required")
        private String type;

        @NotBlank(message = "File URL is required")
        private String url;
    }
}
