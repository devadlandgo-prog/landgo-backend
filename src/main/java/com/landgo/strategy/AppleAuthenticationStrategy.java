package com.landgo.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.landgo.dto.request.RegisterRequest;
import com.landgo.enums.AuthProvider;
import com.landgo.exception.BadRequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AppleAuthenticationStrategy implements OAuth2AuthenticationStrategy {

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    @Value("${app.oauth2.apple.client-id:}")
    private String appleClientId;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AppleAuthenticationStrategy(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.APPLE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2UserInfo extractUserInfo(String token) {
        try {
            // Get Apple's public keys
            Map<String, Object> keysResponse = restTemplate.getForObject(APPLE_KEYS_URL, Map.class);
            List<Map<String, String>> keys = (List<Map<String, String>>) keysResponse.get("keys");

            // Parse the token header to get the key ID
            String[] tokenParts = token.split("\\.");
            String header = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
            Map<String, String> headerMap = objectMapper.readValue(header, Map.class);
            String kid = headerMap.get("kid");

            // Find the matching key
            Map<String, String> matchingKey = keys.stream()
                    .filter(key -> key.get("kid").equals(kid))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Apple key not found"));

            // Build public key
            PublicKey publicKey = buildPublicKey(matchingKey);

            // Verify and parse the token
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Verify audience
            String aud = claims.getAudience().iterator().next();
            if (!appleClientId.equals(aud)) {
                throw new BadRequestException("Invalid Apple token audience");
            }

            String email = claims.get("email", String.class);
            String sub = claims.getSubject();

            // Apple doesn't always provide name, it's only sent on first login
            return OAuth2UserInfo.builder()
                    .providerId(sub)
                    .email(email)
                    .firstName("")
                    .lastName("")
                    .build();

        } catch (Exception e) {
            log.error("Failed to verify Apple token", e);
            throw new BadRequestException("Failed to verify Apple token: " + e.getMessage());
        }
    }

    private PublicKey buildPublicKey(Map<String, String> key) throws Exception {
        String n = key.get("n");
        String e = key.get("e");

        byte[] nBytes = Base64.getUrlDecoder().decode(n);
        byte[] eBytes = Base64.getUrlDecoder().decode(e);

        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    @Override
    public RegisterRequest toRegisterRequest(OAuth2UserInfo userInfo) {
        return RegisterRequest.builder()
                .email(userInfo.getEmail())
                .firstName(userInfo.getFirstName().isEmpty() ? "Apple" : userInfo.getFirstName())
                .lastName(userInfo.getLastName().isEmpty() ? "User" : userInfo.getLastName())
                .profileImageUrl(userInfo.getProfileImageUrl())
                .authProvider(AuthProvider.APPLE)
                .providerId(userInfo.getProviderId())
                .build();
    }
}
