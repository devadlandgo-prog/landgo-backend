package com.landgo.dto.request;

import com.landgo.enums.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Request {

    @NotBlank(message = "Token is required")
    private String token;

    @NotNull(message = "Auth provider is required")
    private AuthProvider authProvider;
}
