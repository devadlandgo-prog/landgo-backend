package com.landgo.service;

import com.landgo.dto.request.SavedSearchRequest;
import com.landgo.dto.response.LandResponse;
import com.landgo.dto.response.PageResponse;
import com.landgo.dto.response.SavedSearchResponse;
import com.landgo.entity.Land;
import com.landgo.entity.SavedSearch;
import com.landgo.entity.User;
import com.landgo.enums.LandStatus;
import com.landgo.enums.ProjectStage;
import com.landgo.exception.BadRequestException;
import com.landgo.exception.ForbiddenException;
import com.landgo.exception.ResourceNotFoundException;
import com.landgo.mapper.LandMapper;
import com.landgo.mapper.SavedSearchMapper;
import com.landgo.repository.LandRepository;
import com.landgo.repository.SavedSearchRepository;
import com.landgo.repository.UserRepository;
import com.landgo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final UserRepository userRepository;
    private final LandRepository landRepository;
    private final SavedSearchMapper savedSearchMapper;
    private final LandMapper landMapper;
    private final EmailService emailService;

    private static final int MAX_SAVED_SEARCHES_PER_USER = 25;

    @Transactional
    public SavedSearchResponse createSavedSearch(UserPrincipal userPrincipal, SavedSearchRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check max limit
        long count = savedSearchRepository.countByUserAndDeletedFalse(user);
        if (count >= MAX_SAVED_SEARCHES_PER_USER) {
            throw new BadRequestException("Maximum " + MAX_SAVED_SEARCHES_PER_USER + " saved searches allowed. Please delete an existing one first.");
        }

        // Check duplicate name
        if (savedSearchRepository.existsByUserAndNameAndDeletedFalse(user, request.getName())) {
            throw new BadRequestException("A saved search with the name '" + request.getName() + "' already exists");
        }

        // Validate at least one criterion is provided
        validateSearchCriteria(request);

        SavedSearch savedSearch = savedSearchMapper.toEntity(request);
        savedSearch.setUser(user);

        // Calculate initial match count
        long matches = countMatches(savedSearch);
        savedSearch.setMatchCount((int) matches);

        savedSearch = savedSearchRepository.save(savedSearch);
        log.info("Saved search created: '{}' by user: {}", request.getName(), user.getEmail());

        return savedSearchMapper.toResponse(savedSearch);
    }

    @Transactional(readOnly = true)
    public PageResponse<SavedSearchResponse> getMySavedSearches(UserPrincipal userPrincipal, Pageable pageable) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Page<SavedSearch> page = savedSearchRepository.findByUserAndDeletedFalseOrderByCreatedAtDesc(user, pageable);

        List<SavedSearchResponse> content = page.getContent().stream()
                .map(savedSearchMapper::toResponse)
                .toList();

        return PageResponse.<SavedSearchResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public SavedSearchResponse getSavedSearchById(UserPrincipal userPrincipal, UUID searchId) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SavedSearch savedSearch = savedSearchRepository.findByIdAndUserAndDeletedFalse(searchId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Saved search not found"));

        return savedSearchMapper.toResponse(savedSearch);
    }

    @Transactional
    public SavedSearchResponse updateSavedSearch(UserPrincipal userPrincipal, UUID searchId, SavedSearchRequest request) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SavedSearch savedSearch = savedSearchRepository.findByIdAndUserAndDeletedFalse(searchId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Saved search not found"));

        // Check duplicate name (if name changed)
        if (!savedSearch.getName().equals(request.getName()) &&
            savedSearchRepository.existsByUserAndNameAndDeletedFalse(user, request.getName())) {
            throw new BadRequestException("A saved search with the name '" + request.getName() + "' already exists");
        }

        validateSearchCriteria(request);

        savedSearch.setName(request.getName());
        savedSearch.setKeyword(request.getKeyword());
        savedSearch.setCity(request.getCity());
        savedSearch.setProjectStage(request.getProjectStage());
        savedSearch.setMinPrice(request.getMinPrice());
        savedSearch.setMaxPrice(request.getMaxPrice());
        savedSearch.setMinLotSize(request.getMinLotSize());
        savedSearch.setMaxLotSize(request.getMaxLotSize());
        savedSearch.setNotificationsEnabled(request.isNotificationsEnabled());

        // Recalculate match count
        long matches = countMatches(savedSearch);
        savedSearch.setMatchCount((int) matches);

        savedSearch = savedSearchRepository.save(savedSearch);
        log.info("Saved search updated: '{}' by user: {}", request.getName(), user.getEmail());

        return savedSearchMapper.toResponse(savedSearch);
    }

    @Transactional
    public void deleteSavedSearch(UserPrincipal userPrincipal, UUID searchId) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SavedSearch savedSearch = savedSearchRepository.findByIdAndUserAndDeletedFalse(searchId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Saved search not found"));

        savedSearch.setDeleted(true);
        savedSearchRepository.save(savedSearch);
        log.info("Saved search deleted: '{}' by user: {}", savedSearch.getName(), user.getEmail());
    }

    @Transactional
    public SavedSearchResponse toggleNotifications(UserPrincipal userPrincipal, UUID searchId) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SavedSearch savedSearch = savedSearchRepository.findByIdAndUserAndDeletedFalse(searchId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Saved search not found"));

        savedSearch.setNotificationsEnabled(!savedSearch.isNotificationsEnabled());
        savedSearch = savedSearchRepository.save(savedSearch);

        log.info("Saved search '{}' notifications {}: user {}",
                savedSearch.getName(),
                savedSearch.isNotificationsEnabled() ? "enabled" : "disabled",
                user.getEmail());

        return savedSearchMapper.toResponse(savedSearch);
    }

    @Transactional(readOnly = true)
    public PageResponse<LandResponse> executeSearch(UserPrincipal userPrincipal, UUID searchId, Pageable pageable) {
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SavedSearch savedSearch = savedSearchRepository.findByIdAndUserAndDeletedFalse(searchId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Saved search not found"));

        Page<Land> results = landRepository.findBySavedSearchCriteria(
                savedSearch.getKeyword(),
                savedSearch.getCity(),
                savedSearch.getProjectStage(),
                savedSearch.getMinPrice(),
                savedSearch.getMaxPrice(),
                savedSearch.getMinLotSize(),
                savedSearch.getMaxLotSize(),
                pageable
        );

        List<LandResponse> content = results.getContent().stream()
                .map(landMapper::toResponse)
                .toList();

        return PageResponse.<LandResponse>builder()
                .content(content)
                .pageNumber(results.getNumber())
                .pageSize(results.getSize())
                .totalElements(results.getTotalElements())
                .totalPages(results.getTotalPages())
                .first(results.isFirst())
                .last(results.isLast())
                .build();
    }

    // ==========================================
    // SCHEDULED NOTIFICATION JOB
    // ==========================================

    /**
     * Runs every 6 hours to check for new matches and notify users.
     */
    @Scheduled(cron = "0 0 */6 * * *")
    @Transactional
    public void checkNewMatchesAndNotify() {
        log.info("Running saved search match notification job...");
        List<SavedSearch> searches = savedSearchRepository.findAllWithNotificationsEnabled();
        int notified = 0;

        for (SavedSearch search : searches) {
            try {
                long currentMatches = countMatches(search);
                int previousMatches = search.getMatchCount();

                if (currentMatches > previousMatches) {
                    long newCount = currentMatches - previousMatches;
                    search.setMatchCount((int) currentMatches);
                    search.setLastNotifiedAt(LocalDateTime.now());
                    savedSearchRepository.save(search);

                    emailService.sendSavedSearchNotification(
                            search.getUser().getEmail(),
                            search.getUser().getFullName(),
                            search.getName(),
                            newCount
                    );
                    notified++;
                } else if (currentMatches != previousMatches) {
                    // Match count decreased (listing removed/sold) — just update count
                    search.setMatchCount((int) currentMatches);
                    savedSearchRepository.save(search);
                }
            } catch (Exception e) {
                log.error("Error processing saved search '{}' for user {}: {}",
                        search.getName(), search.getUser().getEmail(), e.getMessage());
            }
        }

        log.info("Saved search notification job completed. Notified {} users.", notified);
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private long countMatches(SavedSearch search) {
        return landRepository.countBySavedSearchCriteria(
                search.getKeyword(),
                search.getCity(),
                search.getProjectStage(),
                search.getMinPrice(),
                search.getMaxPrice(),
                search.getMinLotSize(),
                search.getMaxLotSize()
        );
    }

    private void validateSearchCriteria(SavedSearchRequest request) {
        boolean hasCriteria = (request.getKeyword() != null && !request.getKeyword().isBlank())
                || (request.getCity() != null && !request.getCity().isBlank())
                || request.getProjectStage() != null
                || request.getMinPrice() != null
                || request.getMaxPrice() != null
                || request.getMinLotSize() != null
                || request.getMaxLotSize() != null;

        if (!hasCriteria) {
            throw new BadRequestException("At least one search criterion is required (keyword, city, projectStage, price range, or lot size range)");
        }

        if (request.getMinPrice() != null && request.getMaxPrice() != null
                && request.getMinPrice().compareTo(request.getMaxPrice()) > 0) {
            throw new BadRequestException("Minimum price cannot be greater than maximum price");
        }

        if (request.getMinLotSize() != null && request.getMaxLotSize() != null
                && request.getMinLotSize().compareTo(request.getMaxLotSize()) > 0) {
            throw new BadRequestException("Minimum lot size cannot be greater than maximum lot size");
        }
    }
}
