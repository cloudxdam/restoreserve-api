package com.pachedev.restoreserve.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pachedev.restoreserve.model.entity.AppUser;
import com.pachedev.restoreserve.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Servicio necesario para que Spring Security use nuestros usuarios de la base
 * de datos en lugar de los que crea automáticamente por defecto.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    /**
     * Busca al usuario en la base de datos por su username y lo convierte
     * en un objeto UserDetails que Spring Security pueda entender.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));

        return User.withUsername(user.getUsername()).password(user.getPassword())
                .authorities(user.getRole().name()).disabled(!user.getActive()).build();
    }

}