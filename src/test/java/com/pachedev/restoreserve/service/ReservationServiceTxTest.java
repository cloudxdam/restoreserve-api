package com.pachedev.restoreserve.service;

import com.pachedev.restoreserve.dto.ReservationRequestDTO;
import com.pachedev.restoreserve.exception.BusinessLogicException;
import com.pachedev.restoreserve.model.entity.AppUser;
import com.pachedev.restoreserve.model.entity.Reservation;
import com.pachedev.restoreserve.model.entity.RestaurantTable;
import com.pachedev.restoreserve.model.enums.ReservationStatus;
import com.pachedev.restoreserve.model.enums.UserStatus;
import com.pachedev.restoreserve.repository.AppUserRepository;
import com.pachedev.restoreserve.repository.ReservationRepository;
import com.pachedev.restoreserve.repository.RestaurantTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:txtestdb")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ReservationServiceTxTest {

    private static final Long SEED_USER_ID = 1L;
    private static final Long SEED_RESERVATION_ID = 1L;
    private static final Long SEED_TABLE_ID = 1L;
    private static final Long FILLER_TABLE_ID = 3L;

    private final ReservationService reservationService;
    private final AppUserRepository appUserRepository;
    private final RestaurantTableRepository tableRepository;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    ReservationServiceTxTest(ReservationService reservationService,
                             AppUserRepository appUserRepository,
                             RestaurantTableRepository tableRepository) {
        this.reservationService = reservationService;
        this.appUserRepository = appUserRepository;
        this.tableRepository = tableRepository;
    }

    @BeforeEach
    void seed() {
        AppUser user = appUserRepository.findById(SEED_USER_ID).orElseThrow();
        user.setPenalizationPoints(0);
        user.setStatus(UserStatus.ACTIVE);
        appUserRepository.save(user);

        Reservation reservation = reservationRepository.findById(SEED_RESERVATION_ID).orElseThrow();
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        RestaurantTable table = tableRepository.findById(SEED_TABLE_ID).orElseThrow();
        table.setIsVip(false);
        tableRepository.save(table);

        if (reservationRepository.findByStatus(ReservationStatus.CONFIRMED).size() <= 3) {
            RestaurantTable filler = tableRepository.findById(FILLER_TABLE_ID).orElseThrow();
            for (int i = 0; i < 4; i++) {
                Reservation r = new Reservation();
                r.setUser(user);
                r.setTable(filler);
                r.setReservationDate(LocalDateTime.now().plusDays(10 + i));
                r.setNumberOfGuests(2);
                r.setStatus(ReservationStatus.CONFIRMED);
                reservationRepository.save(r);
            }
        }
    }

    @Test
    void createPersistsVipFlagOnTable() {
        ReservationRequestDTO dto = new ReservationRequestDTO(
                SEED_TABLE_ID,
                LocalDateTime.now().plusDays(30).withHour(20).withMinute(0).withSecond(0).withNano(0),
                2,
                true);

        reservationService.create(dto, "armymoves");

        RestaurantTable reloaded = tableRepository.findById(SEED_TABLE_ID).orElseThrow();
        assertTrue(reloaded.getIsVip(), "is_vip debe persistirse en restaurant_tables");
    }

    @Test
    @WithMockUser(username = "armymoves")
    void cancelRollsBackPenaltyWhenLastWriteFails() {
        doThrow(new DataIntegrityViolationException("fallo forzado"))
                .when(reservationRepository).save(any(Reservation.class));

        assertThrows(DataIntegrityViolationException.class,
                () -> reservationService.cancelReservation(SEED_RESERVATION_ID));

        AppUser after = appUserRepository.findById(SEED_USER_ID).orElseThrow();
        assertEquals(0, after.getPenalizationPoints(),
                "los puntos deben revertirse si la cancelacion no se completa");
    }

    @Test
    void createRejectsDoubleBookingOfSameSlot() {
        ReservationRequestDTO slot = new ReservationRequestDTO(
                SEED_TABLE_ID,
                LocalDateTime.now().plusDays(45).withHour(21).withMinute(0).withSecond(0).withNano(0),
                2,
                false);

        reservationService.create(slot, "armymoves");

        assertThrows(BusinessLogicException.class,
                () -> reservationService.create(slot, "armymoves"),
                "una segunda reserva en el mismo hueco debe ser rechazada");
    }
}
