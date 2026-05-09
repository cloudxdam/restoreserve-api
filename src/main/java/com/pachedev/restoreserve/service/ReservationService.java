package com.pachedev.restoreserve.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.pachedev.restoreserve.dto.ReservationRequestDTO;
import com.pachedev.restoreserve.dto.ReservationResponseDTO;
import com.pachedev.restoreserve.exception.BusinessLogicException;
import com.pachedev.restoreserve.exception.ResourceNotFoundException;
import com.pachedev.restoreserve.model.entity.Reservation;
import com.pachedev.restoreserve.model.entity.RestaurantTable;
import com.pachedev.restoreserve.model.entity.User;
import com.pachedev.restoreserve.model.enums.ReservationStatus;
import com.pachedev.restoreserve.repository.ReservationRepository;
import com.pachedev.restoreserve.repository.RestaurantTableRepository;
import com.pachedev.restoreserve.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final UserRepository userRepository;

    public ReservationResponseDTO create(ReservationRequestDTO dto) {

        User user = userRepository.findById(dto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con id " + dto.userId() + " no encontrado"));

        RestaurantTable table = tableRepository.findById(dto.tableId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa con id " + dto.tableId() + " no encontrada"));

        if (dto.numberOfGuests() > table.getMaxPax()) {
            throw new BusinessLogicException("El número de comensales introducido supera la capacidad de la mesa");
        }

        Reservation conflicReservation = findConflictingReservation(dto.tableId(), dto.reservationDate());
        if (conflicReservation != null) {
            LocalDateTime start = conflicReservation.getReservationDate();
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

    public List<ReservationResponseDTO> findAll() {
        List<Reservation> reservations = reservationRepository.findAll();
        List<ReservationResponseDTO> response = toResponseList(reservations);

        return response;
    }

    private List<ReservationResponseDTO> toResponseList(List<Reservation> reservations) {
        List<ReservationResponseDTO> response = new ArrayList<>();

        for (Reservation reservation : reservations) {
            response.add(toResponseDTO(reservation));
        }

        return response;

    }

}
