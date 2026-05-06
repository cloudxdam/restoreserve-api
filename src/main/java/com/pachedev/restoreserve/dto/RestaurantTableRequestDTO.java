package com.pachedev.restoreserve.dto;

import com.pachedev.restoreserve.model.enums.TableLocation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RestaurantTableRequestDTO(

        @NotBlank(message = "la numeración de la mesa es obligatoria") String name,
        @NotNull(message = "la cantidad máxima de comensales es obligatoria") @Min(value = 1, message = "Mínimo 1 comensal") Integer maxPax,
        @NotNull(message = "Es obligatorio introducir la zona (SALON o TERRACE)") TableLocation location) {
}