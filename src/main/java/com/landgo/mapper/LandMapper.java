package com.landgo.mapper;

import com.landgo.dto.request.LandCreateRequest;
import com.landgo.dto.response.LandResponse;
import com.landgo.entity.Land;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LandMapper {

    @Mapping(target = "vendorId", source = "vendor.id")
    @Mapping(target = "vendorCompanyName", source = "vendor.companyName")
    @Mapping(target = "vendorVerified", source = "vendor.verified")
    LandResponse toResponse(Land land);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "viewCount", constant = "0")
    @Mapping(target = "inquiryCount", constant = "0")
    Land toEntity(LandCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "vendor", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "inquiryCount", ignore = true)
    void updateEntity(LandCreateRequest request, @MappingTarget Land land);
}
