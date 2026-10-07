package com.pachedev.restoreserve.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservationRequestDTO(
                @NotNull(message = "el id de la mesa es obligatorio") Long tableId,
                @NotNull(message = "la fecha de reserva es obligatoria") @Future(message = "La reserva debe ser en una fecha futura") LocalDateTime reservationDate,
                @NotNull(message = "el número de comensales es obligatorio") @Min(value = 1, message = "El mínimo de comensales es 1") Integer numberOfGuests,
                Boolean isVip) {
}
