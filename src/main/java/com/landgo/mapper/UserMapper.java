package com.landgo.mapper;

import com.landgo.dto.request.RegisterRequest;
import com.landgo.dto.response.UserResponse;
import com.landgo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "isVendor", expression = "java(user.isVendor())")
    @Mapping(target = "isAgent", expression = "java(user.isAgent())")
    @Mapping(target = "fullName", expression = "java(user.getFullName())")
    @Mapping(target = "activeListingsCount", ignore = true)
    @Mapping(target = "totalViews", ignore = true)
    @Mapping(target = "subscriptionPlan", ignore = true)
    @Mapping(target = "subscriptionStatus", ignore = true)
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "vendorProfile", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "savedLands", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "agentAuthorizationAccepted", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "professionalBio", ignore = true)
    User toEntity(RegisterRequest request);
}
