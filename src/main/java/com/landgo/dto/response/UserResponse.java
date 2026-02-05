package com.landgo.dto.response;

import com.landgo.enums.AuthProvider;
import com.landgo.enums.Role;
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
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profileImageUrl;
    private AuthProvider authProvider;
    private Role role;
    private boolean emailVerified;
    private boolean isVendor;
    private LocalDateTime createdAt;
}
