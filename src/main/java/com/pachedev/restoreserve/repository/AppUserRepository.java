package com.pachedev.restoreserve.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.pachedev.restoreserve.model.entity.AppUser;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail (String email);
}