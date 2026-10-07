package com.pachedev.restoreserve.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequestDTO(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotBlank(message = "El nombre de usuario es obligatorio") String username,
        @NotBlank(message = "El teléfono es obligatorio") String telephone,
        @Email(message = "El email introducido no es válido") @NotBlank(message = "El email es obligatorio") String email,
        @NotBlank(message = "La contraseña es obligatoria") String password) {
}
