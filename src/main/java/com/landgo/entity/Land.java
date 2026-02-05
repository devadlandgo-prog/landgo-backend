package com.landgo.entity;

import com.landgo.enums.LandStatus;
import com.landgo.enums.LandType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;

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

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "land_type", nullable = false, length = 50)
    private LandType landType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private LandStatus status = LandStatus.PENDING_APPROVAL;

    // Location
    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "zip_code", nullable = false, length = 20)
    private String zipCode;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "latitude", precision = 10, scale = 8)
    private Double latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private Double longitude;

    // Specifications
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "area_sq_ft", nullable = false, precision = 15, scale = 2)
    private Double areaSqFt;

    @Column(name = "frontage", precision = 10, scale = 2)
    private Double frontage;

    @Column(name = "depth", precision = 10, scale = 2)
    private Double depth;

    // Features
    @Column(name = "has_water_access")
    private boolean hasWaterAccess;

    @Column(name = "has_electricity")
    private boolean hasElectricity;

    @Column(name = "has_road_access")
    private boolean hasRoadAccess;

    @Column(name = "has_sewage")
    private boolean hasSewage;

    @Column(name = "zoning_info")
    private String zoningInfo;

    @Column(name = "topography")
    private String topography;

    @Column(name = "soil_type")
    private String soilType;

    // Media
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "image_urls", columnDefinition = "TEXT[]")
    private List<String> imageUrls;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "virtual_tour_url", length = 500)
    private String virtualTourUrl;

    // Metrics
    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "inquiry_count")
    private Integer inquiryCount = 0;
}
