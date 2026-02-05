package com.landgo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorRegisterRequest {

    @NotBlank(message = "Company name is required")
    @Size(min = 2, max = 100, message = "Company name must be between 2 and 100 characters")
    private String companyName;

    private String companyDescription;

    private String companyLogo;

    private String businessLicense;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    @NotBlank(message = "Business city is required")
    private String businessCity;

    @NotBlank(message = "Business state is required")
    private String businessState;

    @NotBlank(message = "Business zip code is required")
    private String businessZipCode;

    @NotBlank(message = "Business country is required")
    private String businessCountry;

    private String website;
}
