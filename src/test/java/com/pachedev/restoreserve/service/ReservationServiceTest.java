package com.pachedev.restoreserve.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pachedev.restoreserve.dto.ReservationRequestDTO;
import com.pachedev.restoreserve.exception.BannedUserException;
import com.pachedev.restoreserve.exception.BusinessLogicException;
import com.pachedev.restoreserve.model.entity.AppUser;
import com.pachedev.restoreserve.model.entity.Reservation;
import com.pachedev.restoreserve.model.entity.RestaurantTable;
import com.pachedev.restoreserve.model.enums.ReservationStatus;
import com.pachedev.restoreserve.model.enums.TableLocation;
import com.pachedev.restoreserve.model.enums.UserRole;
import com.pachedev.restoreserve.model.enums.UserStatus;
import com.pachedev.restoreserve.repository.AppUserRepository;
import com.pachedev.restoreserve.repository.ReservationRepository;
import com.pachedev.restoreserve.repository.RestaurantTableRepository;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ReservationService reservationService;

    /**
     * Comprobar que no se pueda realizar una reserva con una hora o fecha anterior
     * a la actual
     */
    @Test
    void createPastReservation() {

        AppUser user = new AppUser();
        user.setId(1L);
        user.setName("Tanausú Febles");
        user.setTelephone("600000000");
        user.setEmail("ertana@gmail.com");
        user.setUsername("tana");
        user.setPassword("$2a$12$UfijVlkvrmwpTGERWsfsCe.R5ZIPaJPCP7TmjsnRMYtE8USwgsixi");
        user.setRole(UserRole.ROLE_USER);
        user.setActive(true);
        user.setPenalizationPoints(0);
        user.setStatus(UserStatus.ACTIVE);

        when(appUserRepository.findByUsername("tana")).thenReturn(Optional.of(user));

        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        ReservationRequestDTO dto = new ReservationRequestDTO(1L, pastTime, 4, false);

        assertThrows(BusinessLogicException.class, () -> reservationService.create(dto, "tana"));
    }

    /**
     * Comprobar que no se pueda realizar una reserva con una cantidad de comensales
     * superior a la que tiene la mesa
     */
    @Test
    void createReservationExceedingPax() {

        LocalDateTime date = LocalDateTime.now().plusDays(1);
        ReservationRequestDTO dto = new ReservationRequestDTO(1L, date, 20, false);

        AppUser user = new AppUser();
        user.setId(1L);
        user.setName("Tanausú Febles");
        user.setTelephone("600000000");
        user.setEmail("ertana@gmail.com");
        user.setUsername("tana");
        user.setPassword("$2a$12$UfijVlkvrmwpTGERWsfsCe.R5ZIPaJPCP7TmjsnRMYtE8USwgsixi");
        user.setRole(UserRole.ROLE_USER);
        user.setActive(true);
        user.setPenalizationPoints(0);
        user.setStatus(UserStatus.ACTIVE);

        RestaurantTable table = new RestaurantTable();
        table.setId(1L);
        table.setName("salon 001");
        table.setMaxPax(4);
        table.setLocation(TableLocation.SALON);

        when(appUserRepository.findByUsername("tana")).thenReturn(Optional.of(user));
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));

        assertThrows(BusinessLogicException.class, () -> reservationService.create(dto, "tana"));
    }

    /**
     * Comprobar que un usuario con estado BANNED no pueda realizar una reserva
     */
    @Test
    void createReservationByBannedUser() {

        AppUser user = new AppUser();
        user.setId(1L);
        user.setName("Tanausú Febles");
        user.setTelephone("600000000");
        user.setEmail("ertana@gmail.com");
        user.setUsername("tana");
        user.setPassword("$2a$12$UfijVlkvrmwpTGERWsfsCe.R5ZIPaJPCP7TmjsnRMYtE8USwgsixi");
        user.setRole(UserRole.ROLE_USER);
        user.setActive(true);
        user.setPenalizationPoints(6);
        user.setStatus(UserStatus.BANNED);

        when(appUserRepository.findByUsername("tana")).thenReturn(Optional.of(user));

        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        ReservationRequestDTO dto = new ReservationRequestDTO(1L, pastTime, 4, false);

        assertThrows(BannedUserException.class, () -> reservationService.create(dto, "tana"));
    }

    /**
     * Comprobar que se penaliza con 2 puntos a un usuario que cancela de forma
     * tardía o no aparece a la hora reservada (no-show)
     */
    @Test
    void addPenalizationPointsByLateCancellation() {

        AppUser client = new AppUser();
        client.setId(1L);
        client.setName("Tanausú Febles");
        client.setTelephone("600000000");
        client.setEmail("ertana@gmail.com");
        client.setUsername("tana");
        client.setPassword("$2a$12$UfijVlkvrmwpTGERWsfsCe.R5ZIPaJPCP7TmjsnRMYtE8USwgsixi");
        client.setRole(UserRole.ROLE_USER);
        client.setActive(true);
        client.setPenalizationPoints(0);
        client.setStatus(UserStatus.ACTIVE);

        RestaurantTable table = new RestaurantTable();
        table.setId(1L);
        table.setName("salon 001");
        table.setMaxPax(4);
        table.setLocation(TableLocation.SALON);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(client);
        reservation.setTable(table);
        reservation.setReservationDate(LocalDateTime.now().plusHours(1));
        reservation.setNumberOfGuests(4);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(appUserRepository.findByUsername("tana")).thenReturn(Optional.of(client));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("tana");

        SecurityContextHolder.setContext(securityContext);

        reservationService.cancelReservation(reservation.getId());

        assertEquals(client.getPenalizationPoints(), 2);
        assertEquals(client.getStatus(), UserStatus.ACTIVE);
        assertEquals(reservation.getStatus(), ReservationStatus.CANCELLED);
    }

    /**
     * Comprobar que cambia el estado del cliente a BANNED cuandu supera los 6
     * puntos de penalización por cancelaciones tardías o no aparecer a la hora
     * reservada
     */
    @Test
    void banClientByExceedingSixPenalizationPoints() {

        AppUser client = new AppUser();
        client.setId(1L);
        client.setName("Tanausú Febles");
        client.setTelephone("600000000");
        client.setEmail("ertana@gmail.com");
        client.setUsername("tana");
        client.setPassword("$2a$12$UfijVlkvrmwpTGERWsfsCe.R5ZIPaJPCP7TmjsnRMYtE8USwgsixi");
        client.setRole(UserRole.ROLE_USER);
        client.setActive(true);
        client.setPenalizationPoints(6);
        client.setStatus(UserStatus.ACTIVE);

        RestaurantTable table = new RestaurantTable();
        table.setId(1L);
        table.setName("salon 001");
        table.setMaxPax(4);
        table.setLocation(TableLocation.SALON);

        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(client);
        reservation.setTable(table);
        reservation.setReservationDate(LocalDateTime.now().plusHours(1));
        reservation.setNumberOfGuests(4);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findById(reservation.getId())).thenReturn(Optional.of(reservation));
        when(appUserRepository.findByUsername("tana")).thenReturn(Optional.of(client));

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("tana");

        SecurityContextHolder.setContext(securityContext);

        reservationService.cancelReservation(reservation.getId());

        assertEquals(client.getPenalizationPoints(), 8);
        assertEquals(client.getStatus(), UserStatus.BANNED);
    }
}
