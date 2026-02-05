package com.landgo.service;

import com.landgo.dto.request.LandCreateRequest;
import com.landgo.dto.response.LandResponse;
import com.landgo.dto.response.PageResponse;
import com.landgo.entity.Land;
import com.landgo.entity.VendorProfile;
import com.landgo.enums.LandStatus;
import com.landgo.enums.LandType;
import com.landgo.exception.ForbiddenException;
import com.landgo.exception.ResourceNotFoundException;
import com.landgo.mapper.LandMapper;
import com.landgo.repository.LandRepository;
import com.landgo.repository.VendorRepository;
import com.landgo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LandService {

    private final LandRepository landRepository;
    private final VendorRepository vendorRepository;
    private final LandMapper landMapper;

    @Transactional
    public LandResponse createLand(UserPrincipal userPrincipal, LandCreateRequest request) {
        VendorProfile vendor = vendorRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        Land land = landMapper.toEntity(request);
        land.setVendor(vendor);
        land.setStatus(LandStatus.PENDING_APPROVAL);

        land = landRepository.save(land);
        vendor.incrementLandsListed();
        vendorRepository.save(vendor);

        log.info("Land listing created: {} by vendor: {}", land.getTitle(), vendor.getCompanyName());
        return landMapper.toResponse(land);
    }

    @Transactional(readOnly = true)
    public LandResponse getLandById(UUID landId) {
        Land land = landRepository.findByIdWithVendor(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));
        landRepository.incrementViewCount(landId);
        return landMapper.toResponse(land);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getAllActiveLands(Pageable pageable) {
        Page<Land> landPage = landRepository.findByStatusAndDeletedFalse(LandStatus.ACTIVE, pageable);
        return toPageResponse(landPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> searchLands(String query, Pageable pageable) {
        Page<Land> landPage = landRepository.searchLands(query, pageable);
        return toPageResponse(landPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> filterLands(String city, LandType type,
                                                   BigDecimal minPrice, BigDecimal maxPrice,
                                                   Pageable pageable) {
        Page<Land> landPage = landRepository.findByFilters(city, type, minPrice, maxPrice, pageable);
        return toPageResponse(landPage);
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getRecentListings(int limit) {
        return landRepository.findRecentListings(PageRequest.of(0, limit))
                .stream().map(landMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<LandResponse> getPopularListings(int limit) {
        return landRepository.findPopularListings(PageRequest.of(0, limit))
                .stream().map(landMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> getVendorLands(UserPrincipal userPrincipal, Pageable pageable) {
        VendorProfile vendor = vendorRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));
        Page<Land> landPage = landRepository.findByVendorId(vendor.getId(), pageable);
        return toPageResponse(landPage);
    }

    @Transactional
    public LandResponse updateLand(UserPrincipal userPrincipal, UUID landId, LandCreateRequest request) {
        Land land = landRepository.findByIdWithVendor(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));

        if (!land.getVendor().getUser().getId().equals(userPrincipal.getId())) {
            throw new ForbiddenException("You don't have permission to update this land");
        }

        landMapper.updateEntity(request, land);
        land = landRepository.save(land);
        log.info("Land listing updated: {}", land.getTitle());
        return landMapper.toResponse(land);
    }

    @Transactional
    public void deleteLand(UserPrincipal userPrincipal, UUID landId) {
        Land land = landRepository.findByIdWithVendor(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));

        if (!land.getVendor().getUser().getId().equals(userPrincipal.getId())) {
            throw new ForbiddenException("You don't have permission to delete this land");
        }

        land.setDeleted(true);
        landRepository.save(land);
        log.info("Land listing deleted: {}", land.getTitle());
    }

    @Transactional
    public LandResponse updateLandStatus(UUID landId, LandStatus status) {
        Land land = landRepository.findById(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land", "id", landId));
        land.setStatus(status);
        land = landRepository.save(land);
        log.info("Land status updated: {} -> {}", land.getTitle(), status);
        return landMapper.toResponse(land);
    }

    private PageResponse<LandResponse> toPageResponse(Page<Land> page) {
        return PageResponse.<LandResponse>builder()
                .content(page.getContent().stream().map(landMapper::toResponse).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
