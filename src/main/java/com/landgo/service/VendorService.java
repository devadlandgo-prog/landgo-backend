package com.landgo.service;

import com.landgo.dto.request.VendorRegisterRequest;
import com.landgo.dto.response.PageResponse;
import com.landgo.dto.response.VendorResponse;
import com.landgo.entity.User;
import com.landgo.entity.VendorProfile;
import com.landgo.enums.Role;
import com.landgo.exception.BadRequestException;
import com.landgo.exception.ForbiddenException;
import com.landgo.exception.ResourceNotFoundException;
import com.landgo.mapper.VendorMapper;
import com.landgo.repository.UserRepository;
import com.landgo.repository.VendorRepository;
import com.landgo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final VendorMapper vendorMapper;
    private final SubscriptionService subscriptionService;

    @Transactional
    public VendorResponse registerAsVendor(UserPrincipal userPrincipal, VendorRegisterRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isVendor()) {
            throw new BadRequestException("User is already a vendor");
        }

        VendorProfile vendorProfile = vendorMapper.toEntity(request);
        vendorProfile.setUser(user);

        // Only upgrade role to VENDOR if user is not already an AGENT
        if (user.getRole() != Role.AGENT) {
            user.setRole(Role.VENDOR);
        }
        user.setVendorProfile(vendorProfile);

        userRepository.save(user);
        log.info("User {} registered as vendor: {}", user.getEmail(), request.getCompanyName());

        return vendorMapper.toResponse(vendorProfile);
    }

    @Transactional(readOnly = true)
    public VendorResponse getVendorById(UUID vendorId, UserPrincipal userPrincipal) {
        VendorProfile vendor = vendorRepository.findByIdWithUser(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor", "id", vendorId));

        if (userPrincipal != null) {
            boolean hasAccess = subscriptionService.canAccessVendorDetails(userPrincipal.getId());
            if (!hasAccess) {
                throw new ForbiddenException("Subscription required to view vendor details");
            }
        }

        return vendorMapper.toResponse(vendor);
    }

    @Transactional(readOnly = true)
    public PageResponse<VendorResponse> getAllVendors(Pageable pageable) {
        Page<VendorProfile> vendorPage = vendorRepository.findByVerifiedTrueAndDeletedFalse(pageable);
        return toPageResponse(vendorPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<VendorResponse> searchVendors(String query, Pageable pageable) {
        Page<VendorProfile> vendorPage = vendorRepository.searchVendors(query, pageable);
        return toPageResponse(vendorPage);
    }

    @Transactional(readOnly = true)
    public VendorResponse getMyVendorProfile(UserPrincipal userPrincipal) {
        VendorProfile vendor = vendorRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        return vendorMapper.toResponse(vendor);
    }

    @Transactional
    public VendorResponse updateVendorProfile(UserPrincipal userPrincipal, VendorRegisterRequest request) {
        VendorProfile vendor = vendorRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        vendor.setCompanyName(request.getCompanyName());
        vendor.setCompanyDescription(request.getCompanyDescription());
        vendor.setCompanyLogo(request.getCompanyLogo());
        vendor.setBusinessAddress(request.getBusinessAddress());
        vendor.setBusinessCity(request.getBusinessCity());
        vendor.setBusinessState(request.getBusinessState());
        vendor.setBusinessZipCode(request.getBusinessZipCode());
        vendor.setBusinessCountry(request.getBusinessCountry());
        vendor.setWebsite(request.getWebsite());

        vendor = vendorRepository.save(vendor);
        log.info("Vendor profile updated: {}", vendor.getCompanyName());

        return vendorMapper.toResponse(vendor);
    }

    private PageResponse<VendorResponse> toPageResponse(Page<VendorProfile> page) {
        return PageResponse.<VendorResponse>builder()
                .content(page.getContent().stream().map(vendorMapper::toResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
