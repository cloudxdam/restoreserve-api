package com.pachedev.restoreserve.dto;

import java.time.LocalDateTime;

import com.pachedev.restoreserve.model.enums.ReservationStatus;

public record ReservationResponseDTO(
        Long id,
        String customerName,
        String tableName,
        LocalDateTime dateTime,
        LocalDateTime createdAt,
        Integer numberOfGuests,
        ReservationStatus status) {

}