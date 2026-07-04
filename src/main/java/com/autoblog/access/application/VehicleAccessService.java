package com.autoblog.access.application;

import com.autoblog.access.domain.VehicleAccessRole;
import com.autoblog.access.infrastructure.VehicleAccessEntity;
import com.autoblog.access.infrastructure.VehicleAccessJpaRepository;
import com.autoblog.application.VehicleNotFoundException;
import com.autoblog.identity.application.EmailNormalizer;
import com.autoblog.identity.application.UserAccountNotFoundException;
import com.autoblog.identity.infrastructure.UserAccountEntity;
import com.autoblog.identity.infrastructure.UserAccountJpaRepository;
import com.autoblog.infrastructure.persistence.VehicleEntity;
import com.autoblog.infrastructure.persistence.VehicleJpaRepository;
import com.autoblog.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleAccessService {

    private final VehicleAccessJpaRepository access;
    private final VehicleJpaRepository vehicles;
    private final UserAccountJpaRepository users;
    private final EmailNormalizer emailNormalizer;
    private final CurrentUser currentUser;

    public VehicleAccessService(
            VehicleAccessJpaRepository access,
            VehicleJpaRepository vehicles,
            UserAccountJpaRepository users,
            EmailNormalizer emailNormalizer,
            CurrentUser currentUser
    ) {
        this.access = access;
        this.vehicles = vehicles;
        this.users = users;
        this.emailNormalizer = emailNormalizer;
        this.currentUser = currentUser;
    }

    @Transactional
    public void createOwnerAccess(VehicleEntity vehicle, UUID userId) {
        UserAccountEntity user = users.findById(userId)
                .orElseThrow(() -> new UserAccountNotFoundException(userId.toString()));
        access.save(new VehicleAccessEntity(UUID.randomUUID(), vehicle, user, VehicleAccessRole.OWNER));
    }

    @Transactional(readOnly = true)
    public VehicleAccessRole requireViewAccess(UUID vehicleId) {
        return requireAnyRole(vehicleId, List.of(
                VehicleAccessRole.OWNER,
                VehicleAccessRole.EDITOR,
                VehicleAccessRole.VIEWER
        ));
    }

    @Transactional(readOnly = true)
    public VehicleAccessRole requireEditAccess(UUID vehicleId) {
        return requireAnyRole(vehicleId, List.of(VehicleAccessRole.OWNER, VehicleAccessRole.EDITOR));
    }

    @Transactional(readOnly = true)
    public VehicleAccessRole requireOwnerAccess(UUID vehicleId) {
        return requireAnyRole(vehicleId, List.of(VehicleAccessRole.OWNER));
    }

    @Transactional(readOnly = true)
    public List<VehicleEntity> accessibleVehicles() {
        UUID userId = currentUser.requireUserId();
        return access.findByUser_IdOrderByCreatedAtAsc(userId).stream()
                .map(VehicleAccessEntity::getVehicle)
                .toList();
    }

    @Transactional
    public VehicleAccessView grantAccess(UUID vehicleId, String email, VehicleAccessRole role) {
        requireOwnerAccess(vehicleId);
        if (role == VehicleAccessRole.OWNER) {
            throw new InvalidVehicleAccessException("OWNER access cannot be granted in this endpoint");
        }
        if (role == null) {
            throw new InvalidVehicleAccessException("Role is required");
        }

        VehicleEntity vehicle = vehicles.findById(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
        String normalizedEmail = emailNormalizer.normalize(email);
        UserAccountEntity targetUser = users.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserAccountNotFoundException(normalizedEmail));

        VehicleAccessEntity entry = access.findByVehicle_IdAndUser_Id(vehicleId, targetUser.getId())
                .map(existing -> {
                    existing.setRole(role);
                    return existing;
                })
                .orElseGet(() -> new VehicleAccessEntity(UUID.randomUUID(), vehicle, targetUser, role));

        return toView(access.save(entry));
    }

    @Transactional(readOnly = true)
    public List<VehicleAccessView> listAccess(UUID vehicleId) {
        requireOwnerAccess(vehicleId);
        return access.findByVehicle_IdOrderByCreatedAtAsc(vehicleId).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional
    public void revokeAccess(UUID vehicleId, UUID userId) {
        requireOwnerAccess(vehicleId);
        VehicleAccessEntity entry = access.findByVehicle_IdAndUser_Id(vehicleId, userId)
                .orElseThrow(() -> new VehicleAccessNotFoundException(vehicleId, userId));
        if (entry.getUser().getId().equals(currentUser.requireUserId()) && entry.getRole() == VehicleAccessRole.OWNER) {
            throw new InvalidVehicleAccessException("OWNER cannot revoke their own OWNER access");
        }
        access.delete(entry);
    }

    private VehicleAccessRole requireAnyRole(UUID vehicleId, List<VehicleAccessRole> allowedRoles) {
        UUID userId = currentUser.requireUserId();
        VehicleAccessEntity entry = access.findByVehicle_IdAndUser_Id(vehicleId, userId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));
        if (!allowedRoles.contains(entry.getRole())) {
            throw new VehicleAccessDeniedException("Insufficient vehicle access");
        }
        return entry.getRole();
    }

    private VehicleAccessView toView(VehicleAccessEntity entry) {
        return new VehicleAccessView(
                entry.getId(),
                entry.getVehicle().getId(),
                entry.getUser().getId(),
                entry.getUser().getEmail(),
                entry.getRole(),
                entry.getCreatedAt()
        );
    }
}
