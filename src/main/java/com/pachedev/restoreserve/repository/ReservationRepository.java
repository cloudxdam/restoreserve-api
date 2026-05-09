package com.pachedev.restoreserve.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pachedev.restoreserve.model.entity.Reservation;
import com.pachedev.restoreserve.model.enums.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByStatus(ReservationStatus status);

    List<Reservation> findByTableIdAndStatus(Long tableId, ReservationStatus status);
}
