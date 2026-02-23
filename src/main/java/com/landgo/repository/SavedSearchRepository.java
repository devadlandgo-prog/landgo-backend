package com.landgo.repository;

import com.landgo.entity.SavedSearch;
import com.landgo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, UUID> {

    Page<SavedSearch> findByUserAndDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    List<SavedSearch> findByUserAndDeletedFalse(User user);

    Optional<SavedSearch> findByIdAndUserAndDeletedFalse(UUID id, User user);

    long countByUserAndDeletedFalse(User user);

    @Query("SELECT s FROM SavedSearch s JOIN FETCH s.user WHERE s.notificationsEnabled = true AND s.deleted = false")
    List<SavedSearch> findAllWithNotificationsEnabled();

    boolean existsByUserAndNameAndDeletedFalse(User user, String name);
}
