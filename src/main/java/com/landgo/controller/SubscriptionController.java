package com.landgo.controller;

import com.landgo.dto.request.SubscriptionRequest;
import com.landgo.dto.response.ApiResponse;
import com.landgo.dto.response.SubscriptionResponse;
import com.landgo.security.CurrentUser;
import com.landgo.security.UserPrincipal;
import com.landgo.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Subscription management APIs")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Subscribe to a plan", description = "Subscribe to a subscription plan")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.subscribe(userPrincipal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription successful", response));
    }

    @GetMapping("/current")
    @Operation(summary = "Get current subscription", description = "Get current user's active subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getCurrentSubscription(
            @CurrentUser UserPrincipal userPrincipal) {
        SubscriptionResponse response = subscriptionService.getCurrentSubscription(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel subscription", description = "Cancel current subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(
            @CurrentUser UserPrincipal userPrincipal,
            @RequestParam(required = false) String reason) {
        SubscriptionResponse response = subscriptionService.cancelSubscription(userPrincipal, reason);
        return ResponseEntity.ok(ApiResponse.success("Subscription cancelled successfully", response));
    }
}
