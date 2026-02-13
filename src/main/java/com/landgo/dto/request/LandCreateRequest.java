package com.landgo.dto.request;

import com.landgo.enums.LandType;
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

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    private String description;

    @NotNull(message = "Land type is required")
    private LandType landType;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Zip code is required")
    private String zipCode;

    @NotBlank(message = "Country is required")
    private String country;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Area is required")
    @Positive(message = "Area must be positive")
    private BigDecimal areaSqFt;

    private BigDecimal frontage;

    private BigDecimal depth;

    private boolean hasWaterAccess;

    private boolean hasElectricity;

    private boolean hasRoadAccess;

    private boolean hasSewage;

    private String zoningInfo;

    private String topography;

    private String soilType;

    private List<String> imageUrls;

    private String videoUrl;

    private String virtualTourUrl;
}
