package com.landgo.entity;

import com.landgo.enums.LandStatus;
import com.landgo.enums.ProjectStage;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "lands")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Land extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private VendorProfile vendor;

    // ===== Project Stage =====
    @Enumerated(EnumType.STRING)
    @Column(name = "project_stage", nullable = false, length = 50)
    private ProjectStage projectStage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private LandStatus status = LandStatus.PENDING_APPROVAL;

    // ===== Project Details (Location) =====
    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "lot_size", nullable = false, precision = 15, scale = 2)
    private BigDecimal lotSize;

    @Column(name = "lot_unit", nullable = false, length = 20)
    private String lotUnit;

    @Column(name = "frontage", length = 50)
    private String frontage;

    @Column(name = "depth", length = 50)
    private String depth;

    @Column(name = "current_zoning_codes")
    private String currentZoningCodes;

    @Column(name = "pin_number", length = 50)
    private String pinNumber;

    @Column(name = "official_plan_designation")
    private String officialPlanDesignation;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    // ===== Project Specification (stored as JSONB) =====
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "project_specification", columnDefinition = "jsonb")
    private Map<String, Object> projectSpecification;

    // ===== Pricing =====
    @Column(name = "asking_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal askingPrice;

    @Column(name = "currency", nullable = false, length = 10)
    @Builder.Default
    private String currency = "CAD";

    @Column(name = "pricing_description", columnDefinition = "TEXT")
    private String pricingDescription;

    // ===== Photos (stored as JSONB array) =====
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "photos", columnDefinition = "jsonb")
    private List<Map<String, String>> photos;

    // ===== Documents (stored as JSONB array) =====
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "documents", columnDefinition = "jsonb")
    private List<Map<String, String>> documents;

    // ===== Ownership =====
    @Column(name = "ownership_verification", length = 500)
    private String ownershipVerification;

    // ===== Metrics =====
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "inquiry_count")
    @Builder.Default
    private Integer inquiryCount = 0;
}
