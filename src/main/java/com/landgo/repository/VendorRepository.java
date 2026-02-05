package com.landgo.repository;

import com.landgo.entity.VendorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<VendorProfile, UUID> {

    @Query("SELECT v FROM VendorProfile v JOIN FETCH v.user WHERE v.id = :id AND v.deleted = false")
    Optional<VendorProfile> findByIdWithUser(@Param("id") UUID id);

    @Query("SELECT v FROM VendorProfile v WHERE v.user.id = :userId AND v.deleted = false")
    Optional<VendorProfile> findByUserId(@Param("userId") UUID userId);

    Page<VendorProfile> findByVerifiedTrueAndDeletedFalse(Pageable pageable);

    @Query("SELECT v FROM VendorProfile v WHERE v.deleted = false AND " +
           "(LOWER(v.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.businessCity) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<VendorProfile> searchVendors(@Param("search") String search, Pageable pageable);

    @Query("SELECT v FROM VendorProfile v WHERE v.businessCity = :city AND v.verified = true AND v.deleted = false")
    Page<VendorProfile> findByCity(@Param("city") String city, Pageable pageable);

    long countByVerifiedTrue();
}
