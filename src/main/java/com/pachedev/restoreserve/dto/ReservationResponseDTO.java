package com.pachedev.restoreserve.dto;

import com.pachedev.restoreserve.model.enums.ReservationStatus;

public record ReservationResponseDTO(
        Long id,
        String tableName,
        String customerName,
        String dateTime,
        Integer numberOfGuests,
        ReservationStatus status) {

}