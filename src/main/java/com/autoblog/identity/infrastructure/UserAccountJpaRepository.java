package com.autoblog.identity.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, UUID> {

    boolean existsByEmail(String email);

    Optional<UserAccountEntity> findByEmail(String email);
}
