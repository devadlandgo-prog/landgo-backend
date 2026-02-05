package com.landgo.repository;

import com.landgo.entity.Land;
import com.landgo.enums.LandStatus;
import com.landgo.enums.LandType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LandRepository extends JpaRepository<Land, UUID>, JpaSpecificationExecutor<Land> {

    @Query("SELECT l FROM Land l JOIN FETCH l.vendor v JOIN FETCH v.user WHERE l.id = :id AND l.deleted = false")
    Optional<Land> findByIdWithVendor(@Param("id") UUID id);

    Page<Land> findByStatusAndDeletedFalse(LandStatus status, Pageable pageable);

    @Query("SELECT l FROM Land l WHERE l.vendor.id = :vendorId AND l.deleted = false")
    Page<Land> findByVendorId(@Param("vendorId") UUID vendorId, Pageable pageable);

    @Query("SELECT l FROM Land l WHERE l.status = 'ACTIVE' AND l.deleted = false AND " +
           "(LOWER(l.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.state) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Land> searchLands(@Param("search") String search, Pageable pageable);

    @Query("SELECT l FROM Land l WHERE l.status = 'ACTIVE' AND l.deleted = false AND " +
           "l.city = :city AND l.landType = :type AND " +
           "l.price BETWEEN :minPrice AND :maxPrice")
    Page<Land> findByFilters(@Param("city") String city,
                             @Param("type") LandType type,
                             @Param("minPrice") BigDecimal minPrice,
                             @Param("maxPrice") BigDecimal maxPrice,
                             Pageable pageable);

    @Query("SELECT l FROM Land l WHERE l.status = 'ACTIVE' AND l.deleted = false ORDER BY l.createdAt DESC")
    List<Land> findRecentListings(Pageable pageable);

    @Query("SELECT l FROM Land l WHERE l.status = 'ACTIVE' AND l.deleted = false ORDER BY l.viewCount DESC")
    List<Land> findPopularListings(Pageable pageable);

    @Modifying
    @Query("UPDATE Land l SET l.viewCount = l.viewCount + 1 WHERE l.id = :id")
    void incrementViewCount(@Param("id") UUID id);

    long countByStatus(LandStatus status);
}
