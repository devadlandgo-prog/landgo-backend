package com.landgo.controller;

import com.landgo.dto.request.VendorRegisterRequest;
import com.landgo.dto.response.ApiResponse;
import com.landgo.dto.response.PageResponse;
import com.landgo.dto.response.VendorResponse;
import com.landgo.security.CurrentUser;
import com.landgo.security.UserPrincipal;
import com.landgo.service.VendorService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Vendor", description = "Vendor management APIs")
public class VendorController {

    private final VendorService vendorService;

    @PostMapping("/vendor/register")
    @Operation(summary = "Register as vendor", description = "Register current user as a vendor")
    public ResponseEntity<ApiResponse<VendorResponse>> registerAsVendor(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody VendorRegisterRequest request) {
        VendorResponse response = vendorService.registerAsVendor(userPrincipal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor registration successful", response));
    }

    @GetMapping("/vendors")
    @Operation(summary = "Get all vendors", description = "Get all verified vendors (public)")
    public ResponseEntity<ApiResponse<PageResponse<VendorResponse>>> getAllVendors(
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<VendorResponse> response = vendorService.getAllVendors(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vendors/{id}")
    @Operation(summary = "Get vendor by ID", description = "Get vendor details (requires subscription)")
    public ResponseEntity<ApiResponse<VendorResponse>> getVendorById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal userPrincipal) {
        VendorResponse response = vendorService.getVendorById(id, userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vendors/search")
    @Operation(summary = "Search vendors", description = "Search vendors by name or location")
    public ResponseEntity<ApiResponse<PageResponse<VendorResponse>>> searchVendors(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<VendorResponse> response = vendorService.searchVendors(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vendor/profile")
    @PreAuthorize("hasAnyRole('VENDOR', 'AGENT')")
    @Operation(summary = "Get my vendor profile", description = "Get current vendor's profile")
    public ResponseEntity<ApiResponse<VendorResponse>> getMyVendorProfile(
            @CurrentUser UserPrincipal userPrincipal) {
        VendorResponse response = vendorService.getMyVendorProfile(userPrincipal);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/vendor/profile")
    @PreAuthorize("hasAnyRole('VENDOR', 'AGENT')")
    @Operation(summary = "Update vendor profile", description = "Update current vendor's profile")
    public ResponseEntity<ApiResponse<VendorResponse>> updateVendorProfile(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody VendorRegisterRequest request) {
        VendorResponse response = vendorService.updateVendorProfile(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }
}
