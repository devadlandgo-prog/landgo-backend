package com.landgo.mapper;

import com.landgo.dto.request.VendorRegisterRequest;
import com.landgo.dto.response.VendorResponse;
import com.landgo.entity.VendorProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VendorMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "ownerName", expression = "java(vendor.getUser().getFullName())")
    @Mapping(target = "ownerEmail", source = "user.email")
    VendorResponse toResponse(VendorProfile vendor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "verified", constant = "false")
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "totalReviews", constant = "0")
    @Mapping(target = "totalLandsListed", constant = "0")
    @Mapping(target = "totalLandsSold", constant = "0")
    @Mapping(target = "lands", ignore = true)
    VendorProfile toEntity(VendorRegisterRequest request);
}
