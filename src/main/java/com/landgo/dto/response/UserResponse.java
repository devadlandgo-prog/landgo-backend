package com.landgo.dto.response;

import com.landgo.enums.AuthProvider;
import com.landgo.enums.Role;
import com.landgo.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private UserType userType;
    private String fullName;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profileImageUrl;
    private AuthProvider authProvider;
    private Role role;
    private boolean emailVerified;
    private boolean isVendor;
    private boolean isAgent;

    // Agent-specific fields
    private String agencyName;
    private String recoLicenseNumber;
    private boolean agentAuthorizationAccepted;

    private LocalDateTime createdAt;
}
