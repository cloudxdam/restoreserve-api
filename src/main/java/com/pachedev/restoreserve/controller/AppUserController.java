package com.pachedev.restoreserve.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pachedev.restoreserve.service.AppUserService;

import lombok.RequiredArgsConstructor;

/**
 * Controlador encargado de gestionar las operaciones relacionadas con los
 * usuarios clientes.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class AppUserController {

    private final AppUserService appUserService;

    /**
     * Permite a un administrador resetear la penalización de un usuario.
     */
    @PatchMapping("/{id}/reset-penalization")
    public ResponseEntity<String> resetPenalization(@PathVariable Long id) {
        appUserService.resetPenalization(id);

        return ResponseEntity.ok("Los puntos de penalización del usuario con id " + id
                + " han sido reiniciados y su estado ha pasado a ser ACTIVE");
    }
}
