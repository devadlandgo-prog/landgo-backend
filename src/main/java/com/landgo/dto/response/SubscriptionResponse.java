package com.landgo.dto.response;

import com.landgo.enums.SubscriptionPlan;
import com.landgo.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private UUID id;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal amount;
    private String paymentMethod;
    private boolean autoRenew;
    private boolean isActive;
    
    // Feature limits
    private Integer maxVendorViewsPerMonth;
    private Integer maxSavedLands;
    private boolean canAccessPremiumListings;
    private boolean canContactVendorDirectly;
}
