package com.landgo.controller;

import com.landgo.dto.request.LandCreateRequest;
import com.landgo.dto.response.ApiResponse;
import com.landgo.dto.response.LandResponse;
import com.landgo.dto.response.PageResponse;
import com.landgo.enums.LandType;
import com.landgo.security.CurrentUser;
import com.landgo.security.UserPrincipal;
import com.landgo.service.LandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Land", description = "Land listing management APIs")
public class LandController {

    private final LandService landService;

    // ===== Public Endpoints =====

    @GetMapping("/lands")
    @Operation(summary = "Get all lands", description = "Get all active land listings (public)")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> getAllLands(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<LandResponse> response = landService.getAllActiveLands(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/lands/{id}")
    @Operation(summary = "Get land by ID", description = "Get land details by ID (public)")
    public ResponseEntity<ApiResponse<LandResponse>> getLandById(@PathVariable UUID id) {
        LandResponse response = landService.getLandById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/lands/search")
    @Operation(summary = "Search lands", description = "Search land listings")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> searchLands(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<LandResponse> response = landService.searchLands(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/lands/filter")
    @Operation(summary = "Filter lands", description = "Filter land listings by criteria")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> filterLands(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) LandType type,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<LandResponse> response = landService.filterLands(city, type, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/lands/recent")
    @Operation(summary = "Get recent listings", description = "Get recently listed lands")
    public ResponseEntity<ApiResponse<List<LandResponse>>> getRecentListings(
            @RequestParam(defaultValue = "10") int limit) {
        List<LandResponse> response = landService.getRecentListings(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/lands/popular")
    @Operation(summary = "Get popular listings", description = "Get most viewed land listings")
    public ResponseEntity<ApiResponse<List<LandResponse>>> getPopularListings(
            @RequestParam(defaultValue = "10") int limit) {
        List<LandResponse> response = landService.getPopularListings(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ===== Vendor Endpoints =====

    @PostMapping("/vendor/lands")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Create land listing", description = "Create a new land listing (vendor only)")
    public ResponseEntity<ApiResponse<LandResponse>> createLand(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody LandCreateRequest request) {
        LandResponse response = landService.createLand(userPrincipal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Land listing created successfully", response));
    }

    @GetMapping("/vendor/lands")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Get my lands", description = "Get all lands listed by current vendor")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> getVendorLands(
            @CurrentUser UserPrincipal userPrincipal,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<LandResponse> response = landService.getVendorLands(userPrincipal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/vendor/lands/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Update land listing", description = "Update a land listing (vendor only)")
    public ResponseEntity<ApiResponse<LandResponse>> updateLand(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @Valid @RequestBody LandCreateRequest request) {
        LandResponse response = landService.updateLand(userPrincipal, id, request);
        return ResponseEntity.ok(ApiResponse.success("Land listing updated successfully", response));
    }

    @DeleteMapping("/vendor/lands/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    @Operation(summary = "Delete land listing", description = "Delete a land listing (vendor only)")
    public ResponseEntity<ApiResponse<Void>> deleteLand(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        landService.deleteLand(userPrincipal, id);
        return ResponseEntity.ok(ApiResponse.success("Land listing deleted successfully", null));
    }
}
