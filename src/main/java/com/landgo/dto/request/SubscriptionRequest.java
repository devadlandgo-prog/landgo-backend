package com.landgo.dto.request;

import com.landgo.enums.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {

    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan plan;

    private String paymentMethod;

    private String paymentToken;

    @Builder.Default
    private boolean autoRenew = false;
}
