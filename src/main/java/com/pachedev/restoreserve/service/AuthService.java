package com.pachedev.restoreserve.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pachedev.restoreserve.dto.AuthRequestDTO;
import com.pachedev.restoreserve.dto.AuthResponseDTO;
import com.pachedev.restoreserve.dto.RegisterUserRequestDTO;
import com.pachedev.restoreserve.exception.BusinessLogicException;
import com.pachedev.restoreserve.exception.ResourceNotFoundException;
import com.pachedev.restoreserve.model.entity.AppUser;
import com.pachedev.restoreserve.model.enums.UserRole;
import com.pachedev.restoreserve.repository.AppUserRepository;
import com.pachedev.restoreserve.security.JwtService;

import lombok.RequiredArgsConstructor;

/**
 * Servicio encargado de autenticar usuarios mediante JWT.
 * Comprueba que las credenciales introducidas sean correctas y si el login es
 * válido genera un token para acceder a los endpoints protegidos de la API.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Comprueba las credenciales de un usuario y genera un token JWT.
     * 
     * @param dto el username y la contraseña introducidos por el usuario.
     * @return token JWT generado tras una autenticación correcta.
     */
    public AuthResponseDTO login(AuthRequestDTO dto) {

        AppUser user = appUserRepository.findByUsername(dto.username())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario " + dto.username() + " no encontrado"));

        if (!passwordEncoder.matches(dto.password(), (user.getPassword()))) {
            throw new BusinessLogicException("Credenciales no válidas");
        }

        String token = jwtService.generateToken(user.getUsername());

        return new AuthResponseDTO(token);
    }

    /**
     * Registra un nuevo usuario cliente validando que el nombre de usuario y el
     * email no estén ya en uso.
     *
     * @param dto datos necesarios para registrar el nuevo usuario.
     * @return mensaje de confirmación del registro realizado.
     */
    public String register(RegisterUserRequestDTO dto) {

        if (appUserRepository.existsByUsername(dto.username())) {
            throw new BusinessLogicException(
                    "El nombre de usuario " + dto.username() + " ya está registrado. Elija otro.");
        }

        if (appUserRepository.existsByEmail(dto.email())) {
            throw new BusinessLogicException("El email " + dto.email() + " ya está registrado. Introduzca otro.");
        }

        AppUser user = new AppUser();
        user.setName(dto.name());
        user.setUsername(dto.username());
        user.setTelephone(dto.telephone());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(UserRole.ROLE_USER);
        user.setActive(true);

        appUserRepository.save(user);

        return "El usuario ha sido registrado correctamente";
    }

}
