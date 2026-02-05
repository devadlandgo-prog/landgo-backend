package com.landgo.repository;

import com.landgo.entity.User;
import com.landgo.enums.AuthProvider;
import com.landgo.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndAuthProvider(String email, AuthProvider authProvider);

    Optional<User> findByProviderIdAndAuthProvider(String providerId, AuthProvider authProvider);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.vendorProfile WHERE u.id = :id")
    Optional<User> findByIdWithVendorProfile(@Param("id") UUID id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.subscription WHERE u.id = :id")
    Optional<User> findByIdWithSubscription(@Param("id") UUID id);

    long countByRole(Role role);
}
