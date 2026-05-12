package com.pachedev.restoreserve.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pachedev.restoreserve.dto.ReservationRequestDTO;
import com.pachedev.restoreserve.exception.BusinessLogicException;
import com.pachedev.restoreserve.model.entity.AppUser;
import com.pachedev.restoreserve.model.entity.RestaurantTable;
import com.pachedev.restoreserve.model.enums.TableLocation;
import com.pachedev.restoreserve.model.enums.UserRole;
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

    @InjectMocks
    private ReservationService reservationService;

    /**
     * Comprobar que no se pueda realizar una reserva con una hora o fecha anterior
     * a la actual
     */
    @Test
    void createPastReservation() {

        LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
        ReservationRequestDTO dto = new ReservationRequestDTO(1L, pastTime, 4);

        assertThrows(BusinessLogicException.class, () -> reservationService.create(dto, "tana"));
    }

    /**
     * Comprobar que no se pueda realizar una reserva con una cantidad de comensales
     * superior a la que tiene la mesa
     */
    @Test
    void createReservationExceedingPax() {

        LocalDateTime date = LocalDateTime.now().plusDays(1);
        ReservationRequestDTO dto = new ReservationRequestDTO(1L, date, 20);

        AppUser user = new AppUser();
        user.setId(1L);
        user.setName("Tanausú Febles");
        user.setTelephone("600000000");
        user.setEmail("ertana@gmail.com");
        user.setUsername("tana");
        user.setPassword("$2a$12$UfijVlkvrmwpTGERWsfsCe.R5ZIPaJPCP7TmjsnRMYtE8USwgsixi");
        user.setRole(UserRole.ROLE_USER);
        user.setActive(true);

        RestaurantTable table = new RestaurantTable();
        table.setId(1L);
        table.setName("salon 001");
        table.setMaxPax(4);
        table.setLocation(TableLocation.SALON);
        when(appUserRepository.findByUsername("tana")).thenReturn(Optional.of(user));
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));

        assertThrows(BusinessLogicException.class, () -> reservationService.create(dto, "tana"));
    }
}
