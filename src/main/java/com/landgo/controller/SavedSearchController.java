package com.landgo.controller;

import com.landgo.dto.request.SavedSearchRequest;
import com.landgo.dto.response.ApiResponse;
import com.landgo.dto.response.LandResponse;
import com.landgo.dto.response.PageResponse;
import com.landgo.dto.response.SavedSearchResponse;
import com.landgo.security.CurrentUser;
import com.landgo.security.UserPrincipal;
import com.landgo.service.SavedSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user/saved-searches")
@RequiredArgsConstructor
@Tag(name = "Saved Searches", description = "Save search criteria and get notified when new matching listings appear")
public class SavedSearchController {

    private final SavedSearchService savedSearchService;

    @PostMapping
    @Operation(summary = "Create saved search",
               description = "Save a search with filter criteria. At least one criterion is required. Max 25 per user. Notifications for new matches are enabled by default.")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> createSavedSearch(
            @CurrentUser UserPrincipal userPrincipal,
            @Valid @RequestBody SavedSearchRequest request) {
        SavedSearchResponse response = savedSearchService.createSavedSearch(userPrincipal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Saved search created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get my saved searches",
               description = "Get all saved searches for the current user, ordered by most recent first")
    public ResponseEntity<ApiResponse<PageResponse<SavedSearchResponse>>> getMySavedSearches(
            @CurrentUser UserPrincipal userPrincipal,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<SavedSearchResponse> response = savedSearchService.getMySavedSearches(userPrincipal, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get saved search by ID",
               description = "Get details of a specific saved search")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> getSavedSearchById(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        SavedSearchResponse response = savedSearchService.getSavedSearchById(userPrincipal, id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update saved search",
               description = "Update the name, criteria, or notification settings of a saved search")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> updateSavedSearch(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @Valid @RequestBody SavedSearchRequest request) {
        SavedSearchResponse response = savedSearchService.updateSavedSearch(userPrincipal, id, request);
        return ResponseEntity.ok(ApiResponse.success("Saved search updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete saved search",
               description = "Delete a saved search")
    public ResponseEntity<ApiResponse<Void>> deleteSavedSearch(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        savedSearchService.deleteSavedSearch(userPrincipal, id);
        return ResponseEntity.ok(ApiResponse.success("Saved search deleted successfully", null));
    }

    @PatchMapping("/{id}/notifications")
    @Operation(summary = "Toggle notifications",
               description = "Toggle email notifications on/off for a saved search")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> toggleNotifications(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable UUID id) {
        SavedSearchResponse response = savedSearchService.toggleNotifications(userPrincipal, id);
        String status = response.isNotificationsEnabled() ? "enabled" : "disabled";
        return ResponseEntity.ok(ApiResponse.success("Notifications " + status, response));
    }

    @GetMapping("/{id}/results")
    @Operation(summary = "Execute saved search",
               description = "Run the saved search and return matching active land listings")
    public ResponseEntity<ApiResponse<PageResponse<LandResponse>>> executeSearch(
            @CurrentUser UserPrincipal userPrincipal,
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        PageResponse<LandResponse> response = savedSearchService.executeSearch(userPrincipal, id, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
