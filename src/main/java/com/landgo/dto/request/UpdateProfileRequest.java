package com.landgo.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phone;

    @Size(max = 500, message = "Profile image URL must be at most 500 characters")
    private String profileImageUrl;

    @Size(max = 200, message = "Location must be at most 200 characters")
    private String location;

    @Size(max = 2000, message = "Professional bio must be at most 2000 characters")
    private String professionalBio;
}
