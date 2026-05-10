package com.pachedev.restoreserve.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.pachedev.restoreserve.dto.ReservationRequestDTO;
import com.pachedev.restoreserve.dto.ReservationResponseDTO;
import com.pachedev.restoreserve.exception.BusinessLogicException;
import com.pachedev.restoreserve.exception.ResourceNotFoundException;
import com.pachedev.restoreserve.model.entity.AppUser;
import com.pachedev.restoreserve.model.entity.Reservation;
import com.pachedev.restoreserve.model.entity.RestaurantTable;
import com.pachedev.restoreserve.model.enums.ReservationStatus;
import com.pachedev.restoreserve.repository.AppUserRepository;
import com.pachedev.restoreserve.repository.ReservationRepository;
import com.pachedev.restoreserve.repository.RestaurantTableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final AppUserRepository appUserRepository;

    public ReservationResponseDTO create(ReservationRequestDTO dto) {

        AppUser user = appUserRepository.findById(dto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con id " + dto.userId() + " no encontrado"));

        RestaurantTable table = tableRepository.findById(dto.tableId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa con id " + dto.tableId() + " no encontrada"));

        if (dto.numberOfGuests() > table.getMaxPax()) {
            throw new BusinessLogicException("El número de comensales introducido supera la capacidad de la mesa");
        }

        Reservation conflictingReservation = findConflictingReservation(dto.tableId(), dto.reservationDate());
        if (conflictingReservation != null) {
            LocalDateTime start = conflictingReservation.getReservationDate();
            LocalDateTime end = start.plusHours(2);
            throw new BusinessLogicException("La mesa con id " + dto.tableId() + " está reservada de "
                    + start.toLocalTime() + " a " + end.toLocalTime()
                    + ". Seleccione otra hora.");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setTable(table);
        reservation.setReservationDate(dto.reservationDate());
        reservation.setNumberOfGuests(dto.numberOfGuests());
        reservation.setStatus(ReservationStatus.CONFIRMED);

        Reservation savedReservation = reservationRepository.save(reservation);

        return toResponseDTO(savedReservation);
    }

    private ReservationResponseDTO toResponseDTO(Reservation reservation) {
        return new ReservationResponseDTO(reservation.getId(), reservation.getUser().getName(),
                reservation.getTable().getName(), reservation.getReservationDate(), reservation.getCreatedAt(),
                reservation.getNumberOfGuests(), reservation.getStatus());

    }

    private Reservation findConflictingReservation(Long tableId, LocalDateTime requestedReservationTime) {
        List<Reservation> confirmedReservations = reservationRepository.findByTableIdAndStatus(tableId,
                ReservationStatus.CONFIRMED);

        LocalDateTime requestedReservationEnd = requestedReservationTime.plusHours(2);

        for (Reservation reservation : confirmedReservations) {
            LocalDateTime confirmedReservationTime = reservation.getReservationDate();
            LocalDateTime confirmedReservationEnd = confirmedReservationTime.plusHours(2);

            if (requestedReservationTime.isBefore(confirmedReservationEnd)
                    && requestedReservationEnd.isAfter(confirmedReservationTime)) {
                return reservation;
            }
        }

        return null;
    }

    /**
     * Devuelve las reservas según el rol del usuario autenticado.
     * 
     * ROLE_ADMIN -> todas las reservas.
     * ROLE_USER -> solo sus reservas.
     */
    public List<ReservationResponseDTO> findAll() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Reservation> reservations = new ArrayList<>();

        if (user.getRole().name().equals("ROLE_ADMIN")) {
            reservations = reservationRepository.findAll();
        } else {
            reservations = reservationRepository.findByUserId(user.getId());
        }
        return toResponseList(reservations);
    }

    private List<ReservationResponseDTO> toResponseList(List<Reservation> reservations) {
        List<ReservationResponseDTO> response = new ArrayList<>();

        for (Reservation reservation : reservations) {
            response.add(toResponseDTO(reservation));
        }

        return response;
    }

    public ReservationResponseDTO findById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva con id " + id + " no encontrada"));

        return toResponseDTO(reservation);
    }

    public List<ReservationResponseDTO> findByUserId(Long userId) {
        if (!appUserRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario con id " + userId + " no encontrado");
        }

        List<Reservation> reservations = reservationRepository.findByUserId(userId);

        return toResponseList(reservations);
    }

    public List<ReservationResponseDTO> findByStatus(ReservationStatus status) {
        List<Reservation> reservations = reservationRepository.findByStatus(status);

        return toResponseList(reservations);
    }

    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva con id " + id + " no encontrada"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessLogicException("La reserva con id " + id + " ya estaba cancelada");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        reservationRepository.save(reservation);

    }
}
