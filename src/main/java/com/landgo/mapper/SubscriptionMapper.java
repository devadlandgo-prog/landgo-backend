package com.landgo.mapper;

import com.landgo.dto.response.SubscriptionResponse;
import com.landgo.entity.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "isActive", expression = "java(subscription.isActive())")
    SubscriptionResponse toResponse(Subscription subscription);
}
