package com.pachedev.restoreserve.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pachedev.restoreserve.dto.ReservationRequestDTO;
import com.pachedev.restoreserve.dto.ReservationResponseDTO;
import com.pachedev.restoreserve.model.enums.ReservationStatus;
import com.pachedev.restoreserve.service.ReservationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controlador encargado de gestionar las operaciones relacionadas con las
 * reservas.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * Crea una nueva reserva asociándola al usuario autenticado.
     */
    @PostMapping
    public ResponseEntity<ReservationResponseDTO> create(@Valid @RequestBody ReservationRequestDTO dto) {

        // Obtenemos el usuario del contexto de seguridad (JWT)
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        ReservationResponseDTO response = reservationService.create(dto, currentUsername);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Devuelve las reservas visibles para el usuario autenticado según su rol.
     */
    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> findAll() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    /**
     * Busca una reserva por su identificador si el usuario tiene permisos para
     * verla.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    /**
     * Devuelve las reservas filtradas por estado según el rol del usuario
     * autenticado.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReservationResponseDTO>> findByStatus(@PathVariable ReservationStatus status) {
        return ResponseEntity.ok(reservationService.findByStatus(status));
    }

    /**
     * Cancela una reserva existente cambiando su estado a CANCELLED.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);

        return ResponseEntity.noContent().build();
    }
}
