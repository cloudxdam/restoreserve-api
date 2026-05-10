package com.pachedev.restoreserve.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(
        @NotBlank(message = "El nombre de usuario es obligatorio") String username,
        @NotBlank(message = "El password es obligatorio") String password) {
}