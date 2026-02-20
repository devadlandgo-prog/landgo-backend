package com.landgo.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.landgo.dto.request.LandCreateRequest;
import com.landgo.dto.response.LandResponse;
import com.landgo.entity.Land;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class LandMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public LandResponse toResponse(Land land) {
        if (land == null) return null;

        LandResponse.LandResponseBuilder builder = LandResponse.builder()
                .id(land.getId())
                .projectStage(land.getProjectStage())
                .status(land.getStatus())
                // Project Details
                .address(land.getAddress())
                .city(land.getCity())
                .postalCode(land.getPostalCode())
                .lotSize(land.getLotSize())
                .lotUnit(land.getLotUnit())
                .frontage(land.getFrontage())
                .depth(land.getDepth())
                .currentZoningCodes(land.getCurrentZoningCodes())
                .pinNumber(land.getPinNumber())
                .officialPlanDesignation(land.getOfficialPlanDesignation())
                .latitude(land.getLatitude())
                .longitude(land.getLongitude())
                // Project Specification
                .projectSpecification(land.getProjectSpecification())
                // Pricing
                .askingPrice(land.getAskingPrice())
                .currency(land.getCurrency())
                .pricingDescription(land.getPricingDescription())
                // Media
                .photos(land.getPhotos())
                .documents(land.getDocuments())
                // Ownership
                .ownershipVerification(land.getOwnershipVerification())
                // Metrics
                .viewCount(land.getViewCount())
                .inquiryCount(land.getInquiryCount())
                // Timestamps
                .createdAt(land.getCreatedAt())
                .updatedAt(land.getUpdatedAt());

        // Vendor info
        if (land.getVendor() != null) {
            builder.vendorId(land.getVendor().getId())
                   .vendorCompanyName(land.getVendor().getCompanyName())
                   .vendorVerified(land.getVendor().isVerified());
        }

        return builder.build();
    }

    public Land toEntity(LandCreateRequest request) {
        if (request == null) return null;

        Land land = new Land();
        land.setProjectStage(request.getProjectStage());
        land.setAskingPrice(request.getPricing().getAskingPrice());
        land.setCurrency(request.getPricing().getCurrency());
        land.setPricingDescription(request.getPricing().getDescription());
        land.setOwnershipVerification(request.getOwnershipVerification());
        land.setViewCount(0);
        land.setInquiryCount(0);

        // Map project details
        LandCreateRequest.ProjectDetailsDto details = request.getProjectDetails();
        if (details != null) {
            land.setAddress(details.getAddress());
            land.setCity(details.getCity());
            land.setPostalCode(details.getPostalCode());
            land.setLotSize(details.getLotSize());
            land.setLotUnit(details.getLotUnit());
            land.setFrontage(details.getFrontage());
            land.setDepth(details.getDepth());
            land.setCurrentZoningCodes(details.getCurrentZoningCodes());
            land.setPinNumber(details.getPinNumber());
            land.setOfficialPlanDesignation(details.getOfficialPlanDesignation());

            if (details.getCoordinates() != null) {
                land.setLatitude(details.getCoordinates().getLat());
                land.setLongitude(details.getCoordinates().getLng());
            }
        }

        // Map project specification to JSON map
        if (request.getProjectSpecification() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> specMap = objectMapper.convertValue(request.getProjectSpecification(), Map.class);
            // Remove null values
            specMap.values().removeIf(Objects::isNull);
            land.setProjectSpecification(specMap);
        }

        // Map photos
        if (request.getPhotos() != null) {
            land.setPhotos(request.getPhotos().stream()
                    .map(f -> {
                        Map<String, String> m = new LinkedHashMap<>();
                        m.put("name", f.getName());
                        m.put("type", f.getType());
                        m.put("url", f.getUrl());
                        return m;
                    }).collect(Collectors.toList()));
        }

        // Map documents
        if (request.getDocuments() != null) {
            land.setDocuments(request.getDocuments().stream()
                    .map(f -> {
                        Map<String, String> m = new LinkedHashMap<>();
                        m.put("name", f.getName());
                        m.put("type", f.getType());
                        m.put("url", f.getUrl());
                        return m;
                    }).collect(Collectors.toList()));
        }

        return land;
    }

    public void updateEntity(LandCreateRequest request, Land land) {
        if (request == null || land == null) return;

        land.setProjectStage(request.getProjectStage());

        // Update project details
        LandCreateRequest.ProjectDetailsDto details = request.getProjectDetails();
        if (details != null) {
            land.setAddress(details.getAddress());
            land.setCity(details.getCity());
            land.setPostalCode(details.getPostalCode());
            land.setLotSize(details.getLotSize());
            land.setLotUnit(details.getLotUnit());
            land.setFrontage(details.getFrontage());
            land.setDepth(details.getDepth());
            land.setCurrentZoningCodes(details.getCurrentZoningCodes());
            land.setPinNumber(details.getPinNumber());
            land.setOfficialPlanDesignation(details.getOfficialPlanDesignation());

            if (details.getCoordinates() != null) {
                land.setLatitude(details.getCoordinates().getLat());
                land.setLongitude(details.getCoordinates().getLng());
            }
        }

        // Update project specification
        if (request.getProjectSpecification() != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> specMap = objectMapper.convertValue(request.getProjectSpecification(), Map.class);
            specMap.values().removeIf(Objects::isNull);
            land.setProjectSpecification(specMap);
        }

        // Update pricing
        if (request.getPricing() != null) {
            land.setAskingPrice(request.getPricing().getAskingPrice());
            land.setCurrency(request.getPricing().getCurrency());
            land.setPricingDescription(request.getPricing().getDescription());
        }

        // Update photos
        if (request.getPhotos() != null) {
            land.setPhotos(request.getPhotos().stream()
                    .map(f -> {
                        Map<String, String> m = new LinkedHashMap<>();
                        m.put("name", f.getName());
                        m.put("type", f.getType());
                        m.put("url", f.getUrl());
                        return m;
                    }).collect(Collectors.toList()));
        }

        // Update documents
        if (request.getDocuments() != null) {
            land.setDocuments(request.getDocuments().stream()
                    .map(f -> {
                        Map<String, String> m = new LinkedHashMap<>();
                        m.put("name", f.getName());
                        m.put("type", f.getType());
                        m.put("url", f.getUrl());
                        return m;
                    }).collect(Collectors.toList()));
        }

        land.setOwnershipVerification(request.getOwnershipVerification());
    }
}
