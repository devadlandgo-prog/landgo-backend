package com.landgo.entity;

import com.landgo.enums.AuthProvider;
import com.landgo.enums.Role;
import com.landgo.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    @Builder.Default
    private UserType userType = UserType.SELLER;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.EMAIL;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.SELLER;

    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "active")
    @Builder.Default
    private boolean active = true;

    // --- Agent-specific fields ---

    @Column(name = "agency_name", length = 200)
    private String agencyName;

    @Column(name = "reco_license_number", length = 50)
    private String recoLicenseNumber;

    @Column(name = "agent_authorization_accepted")
    @Builder.Default
    private boolean agentAuthorizationAccepted = false;

    // --- Relationships ---

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private VendorProfile vendorProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Subscription subscription;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_saved_lands",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "land_id")
    )
    @Builder.Default
    private Set<Land> savedLands = new HashSet<>();

    public String getFullName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public boolean isVendor() {
        return role == Role.VENDOR || vendorProfile != null;
    }

    public boolean isAgent() {
        return userType == UserType.AGENT || role == Role.AGENT;
    }

    public boolean canListLands() {
        return (role == Role.VENDOR || role == Role.AGENT) && vendorProfile != null;
    }
}
