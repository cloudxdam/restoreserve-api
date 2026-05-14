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
import com.pachedev.restoreserve.model.enums.UserStatus;
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

    /**
     * Crea una reserva validando:
     * - que la fecha sea en el futuro
     * - capacidad de la mesa
     * - que una mesa previamente reservada estará ocupada por dos horas, por lo
     * que la hora de reserva solicitada no entrará en conflicto.
     */
    public ReservationResponseDTO create(ReservationRequestDTO dto, String currentUsername) {

        if (dto.reservationDate().isBefore(LocalDateTime.now())) {
            throw new BusinessLogicException("La fecha y hora no pueden ser anteriores a la actual");
        }

        AppUser user = appUserRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + currentUsername + " no encontrado"));

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

    /**
     * Comprueba si ya existe una reserva confirmada en la misma mesa dentro de la
     * franja horaria de 2 horas.
     */
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

        AppUser user = getUser();

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

    /**
     * Devuelve una reserva por su id si el usuario autenticado tiene permisos para
     * verla.
     */
    public ReservationResponseDTO findById(Long id) {
        Reservation reservation = getReservation(id);

        AppUser user = getUser();

        validateReservationAccess(reservation, user);

        return toResponseDTO(reservation);
    }

    /**
     * Devuelve las reservas con el estado indicado aplicando restricciones según el
     * rol del usuario autenticado.
     */
    public List<ReservationResponseDTO> findByStatus(ReservationStatus status) {
        AppUser user = getUser();

        List<Reservation> reservations = new ArrayList<>();

        if (user.getRole().name().equals("ROLE_ADMIN")) {
            reservations = reservationRepository.findByStatus(status);
        } else {
            reservations = reservationRepository.findByUserIdAndStatus(user.getId(), status);
        }

        return toResponseList(reservations);
    }

    /**
     * Cancela una reserva si el usuario autenticado tiene permisos para
     * modificarla.
     * 
     * En caso de cancelación tardía o no-show, le sumamos 2 puntos de penalización
     * al cliente.
     * Cuando acumule más de 6 puntos, su estado pasará de ACTIVE a BANNED.
     */
    public void cancelReservation(Long id) {
        Reservation reservation = getReservation(id);

        AppUser user = getUser();

        validateReservationAccess(reservation, user);

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessLogicException("La reserva con id " + id + " ya estaba cancelada");
        }

        LocalDateTime reservationTime = reservation.getReservationDate();
        LocalDateTime limit = reservationTime.minusHours(2);

        AppUser client = reservation.getUser();

        if (LocalDateTime.now().isAfter(limit)) {
            client.setPenalizationPoints(client.getPenalizationPoints() + 2);

            if (client.getPenalizationPoints() > 6) {
                client.setStatus(UserStatus.BANNED);
            }

            appUserRepository.save(client);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        reservationRepository.save(reservation);
    }

    /**
     * Verifica que el usuario autenticado pueda acceder o modificar la reserva
     * indicada.
     */
    private void validateReservationAccess(Reservation reservation, AppUser user) {
        if (!user.getRole().name().equals("ROLE_ADMIN") && !reservation.getUser().getId().equals(user.getId())) {
            throw new BusinessLogicException("Su usuario no tiene permisos para acceder a la reserva especificada");
        }
    }

    /**
     * Obtiene el usuario autenticado actual a partir del contexto de seguridad.
     */
    private AppUser getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + username + " no encontrado"));
        return user;
    }

    /**
     * Busca una reserva por su identificador o lanza una excepción si no existe.
     */
    private Reservation getReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva con id " + id + " no encontrada"));
        return reservation;
    }
}
