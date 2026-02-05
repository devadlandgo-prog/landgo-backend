package com.landgo.service;

import com.landgo.dto.request.SubscriptionRequest;
import com.landgo.dto.response.SubscriptionResponse;
import com.landgo.entity.Subscription;
import com.landgo.entity.User;
import com.landgo.enums.SubscriptionPlan;
import com.landgo.enums.SubscriptionStatus;
import com.landgo.exception.BadRequestException;
import com.landgo.exception.ResourceNotFoundException;
import com.landgo.mapper.SubscriptionMapper;
import com.landgo.repository.SubscriptionRepository;
import com.landgo.repository.UserRepository;
import com.landgo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionMapper subscriptionMapper;

    private record SubscriptionConfig(
            BigDecimal price, int maxVendorViews, int maxSavedLands,
            boolean canAccessPremium, boolean canContactVendor, int durationDays
    ) {}

    private static final Map<SubscriptionPlan, SubscriptionConfig> PLAN_CONFIGS = Map.of(
            SubscriptionPlan.FREE, new SubscriptionConfig(BigDecimal.ZERO, 5, 10, false, false, 0),
            SubscriptionPlan.BASIC, new SubscriptionConfig(new BigDecimal("9.99"), 20, 50, false, true, 30),
            SubscriptionPlan.PREMIUM, new SubscriptionConfig(new BigDecimal("29.99"), 100, 200, true, true, 30),
            SubscriptionPlan.ENTERPRISE, new SubscriptionConfig(new BigDecimal("99.99"), -1, -1, true, true, 365)
    );

    @Transactional
    public SubscriptionResponse subscribe(UserPrincipal userPrincipal, SubscriptionRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        subscriptionRepository.findActiveByUserId(user.getId())
                .ifPresent(sub -> { throw new BadRequestException("User already has an active subscription"); });

        SubscriptionConfig config = PLAN_CONFIGS.get(request.getPlan());
        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(request.getPlan())
                .status(SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(now.plusDays(config.durationDays() == 0 ? 36500 : config.durationDays()))
                .amount(config.price())
                .paymentMethod(request.getPaymentMethod())
                .autoRenew(request.isAutoRenew())
                .maxVendorViewsPerMonth(config.maxVendorViews())
                .maxSavedLands(config.maxSavedLands())
                .canAccessPremiumListings(config.canAccessPremium())
                .canContactVendorDirectly(config.canContactVendor())
                .build();

        subscription = subscriptionRepository.save(subscription);
        log.info("User {} subscribed to plan: {}", user.getEmail(), request.getPlan());
        return subscriptionMapper.toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getCurrentSubscription(UserPrincipal userPrincipal) {
        Subscription subscription = subscriptionRepository.findActiveByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));
        return subscriptionMapper.toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(UserPrincipal userPrincipal, String reason) {
        Subscription subscription = subscriptionRepository.findActiveByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancellationReason(reason);
        subscription.setAutoRenew(false);

        subscription = subscriptionRepository.save(subscription);
        log.info("Subscription cancelled for user: {}", userPrincipal.getId());
        return subscriptionMapper.toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public boolean canAccessVendorDetails(UUID userId) {
        return subscriptionRepository.findActiveByUserId(userId)
                .map(sub -> sub.getPlan() != SubscriptionPlan.FREE)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(UUID userId) {
        return subscriptionRepository.findActiveByUserId(userId).isPresent();
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processExpiredSubscriptions() {
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
        for (Subscription subscription : expired) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(subscription);
            log.info("Subscription expired for user: {}", subscription.getUser().getId());
        }
    }
}
