package com.landgo.repository;

import com.landgo.entity.Land;
import com.landgo.enums.LandStatus;
import com.landgo.enums.ProjectStage;
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
           "(LOWER(l.address) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(l.city) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Land> searchLands(@Param("search") String search, Pageable pageable);

    @Query("SELECT l FROM Land l WHERE l.status = 'ACTIVE' AND l.deleted = false AND " +
           "(:city IS NULL OR l.city = :city) AND " +
           "(:stage IS NULL OR l.projectStage = :stage) AND " +
           "(:minPrice IS NULL OR l.askingPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.askingPrice <= :maxPrice)")
    Page<Land> findByFilters(@Param("city") String city,
                             @Param("stage") ProjectStage stage,
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

    @Query("SELECT COUNT(l) FROM Land l WHERE l.vendor.user.id = :userId AND l.status = 'ACTIVE' AND l.deleted = false")
    int countActiveListingsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(l.viewCount), 0) FROM Land l WHERE l.vendor.user.id = :userId AND l.deleted = false")
    long sumViewCountByUserId(@Param("userId") UUID userId);

    // --- Saved Search queries ---

    @Query("SELECT l FROM Land l WHERE l.status = 'ACTIVE' AND l.deleted = false AND " +
           "(:keyword IS NULL OR (LOWER(l.address) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR LOWER(l.city) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))) AND " +
           "(:city IS NULL OR LOWER(l.city) = LOWER(CAST(:city AS string))) AND " +
           "(:stage IS NULL OR l.projectStage = :stage) AND " +
           "(:minPrice IS NULL OR l.askingPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.askingPrice <= :maxPrice) AND " +
           "(:minLotSize IS NULL OR l.lotSize >= :minLotSize) AND " +
           "(:maxLotSize IS NULL OR l.lotSize <= :maxLotSize)")
    Page<Land> findBySavedSearchCriteria(
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("stage") ProjectStage stage,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minLotSize") BigDecimal minLotSize,
            @Param("maxLotSize") BigDecimal maxLotSize,
            Pageable pageable);

    @Query("SELECT COUNT(l) FROM Land l WHERE l.status = 'ACTIVE' AND l.deleted = false AND " +
           "(:keyword IS NULL OR (LOWER(l.address) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')) OR LOWER(l.city) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))) AND " +
           "(:city IS NULL OR LOWER(l.city) = LOWER(CAST(:city AS string))) AND " +
           "(:stage IS NULL OR l.projectStage = :stage) AND " +
           "(:minPrice IS NULL OR l.askingPrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR l.askingPrice <= :maxPrice) AND " +
           "(:minLotSize IS NULL OR l.lotSize >= :minLotSize) AND " +
           "(:maxLotSize IS NULL OR l.lotSize <= :maxLotSize)")
    long countBySavedSearchCriteria(
            @Param("keyword") String keyword,
            @Param("city") String city,
            @Param("stage") ProjectStage stage,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minLotSize") BigDecimal minLotSize,
            @Param("maxLotSize") BigDecimal maxLotSize);

    long countByStatus(LandStatus status);
}
