package com.landgo.dto.request;

import com.landgo.enums.AuthProvider;
import com.landgo.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotNull(message = "User type is required (SELLER or AGENT)")
    private UserType userType;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private String phone;

    // --- Agent-specific fields ---

    @Size(max = 200, message = "Agency name must be at most 200 characters")
    private String agencyName;

    @Size(max = 50, message = "RECO license number must be at most 50 characters")
    private String recoLicenseNumber;

    private Boolean agentAuthorizationAccepted;

    // --- OAuth2 / Internal fields ---

    @Builder.Default
    private AuthProvider authProvider = AuthProvider.EMAIL;

    private String providerId;

    private String profileImageUrl;

    // --- Kept for backward compatibility / internal mapper use ---

    private String firstName;

    private String lastName;
}
