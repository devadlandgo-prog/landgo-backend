package com.landgo.strategy;

import com.landgo.dto.request.RegisterRequest;
import com.landgo.enums.AuthProvider;

/**
 * Strategy Pattern: Interface for OAuth authentication providers
 * Follows Open/Closed Principle - open for extension, closed for modification
 */
public interface OAuth2AuthenticationStrategy {

    AuthProvider getProvider();

    OAuth2UserInfo extractUserInfo(String token);

    RegisterRequest toRegisterRequest(OAuth2UserInfo userInfo);
}
