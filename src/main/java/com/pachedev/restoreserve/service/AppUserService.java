package com.pachedev.restoreserve.service;

import org.springframework.stereotype.Service;

import com.pachedev.restoreserve.exception.ResourceNotFoundException;
import com.pachedev.restoreserve.model.entity.AppUser;
import com.pachedev.restoreserve.model.enums.UserStatus;
import com.pachedev.restoreserve.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;

    /**
     * Reinicia los puntos de penalización de un usuario y cambia su estado a
     * ACTIVE.
     */
    public void resetPenalization(Long id) {

        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con id " + id + " no encontrado"));

        user.setPenalizationPoints(0);
        user.setStatus(UserStatus.ACTIVE);

        appUserRepository.save(user);
    }
}
