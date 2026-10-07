package com.pachedev.restoreserve.config;

import com.pachedev.restoreserve.model.entity.AppUser;
import com.pachedev.restoreserve.model.entity.Reservation;
import com.pachedev.restoreserve.model.entity.RestaurantTable;
import com.pachedev.restoreserve.model.enums.*;
import com.pachedev.restoreserve.repository.AppUserRepository;
import com.pachedev.restoreserve.repository.ReservationRepository;
import com.pachedev.restoreserve.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final RestaurantTableRepository restaurantTableRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        RestaurantTable mesa1 = createTable("Mesa 1", 4, TableStatus.AVAILABLE, TableLocation.SALON, false);
        RestaurantTable mesa2 = createTable("Mesa 2", 2, TableStatus.RESERVED, TableLocation.TERRACE, false);
        RestaurantTable mesa3 = createTable("Mesa 3", 6, TableStatus.AVAILABLE, TableLocation.SALON, false);
        RestaurantTable mesa4 = createTable("Mesa 4", 8, TableStatus.RESERVED, TableLocation.TERRACE, false);

        AppUser armymoves = createUser("Armiche Santana", "6123456789", "armiche@gmail.com", "armymoves",
                "$2a$12$UfijVlkvrmwpTGERWsfsCe.R5ZIPaJPCP7TmjsnRMYtE8USwgsixi", UserRole.ROLE_USER);
        AppUser admin = createUser("Acaymo Bello", "600000000", "acaymo@gmail.com", "admin",
                "$2a$12$lAUz1WTEed/7SOsy.ew.OehjCeESngVWkgkc0fqRgZqeo/W4AwM..", UserRole.ROLE_ADMIN);

        createReservation(armymoves, mesa1, LocalDateTime.of(2026, 5, 16, 20, 0), 2);
        createReservation(admin, mesa2, LocalDateTime.of(2026, 5, 16, 15, 0), 2);
    }

    private RestaurantTable createTable(String name, Integer maxPax, TableStatus status,
                                        TableLocation location, Boolean isVip) {
        RestaurantTable table = new RestaurantTable();
        table.setName(name);
        table.setMaxPax(maxPax);
        table.setStatus(status);
        table.setLocation(location);
        table.setIsVip(isVip);
        return restaurantTableRepository.save(table);
    }

    private AppUser createUser(String name, String telephone, String email, String username,
                               String password, UserRole role) {
        AppUser user = new AppUser();
        user.setName(name);
        user.setTelephone(telephone);
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setPenalizationPoints(0);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private void createReservation(AppUser user, RestaurantTable table,
                                   LocalDateTime reservationDate, Integer numberOfGuests) {
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setTable(table);
        reservation.setReservationDate(reservationDate);
        reservation.setNumberOfGuests(numberOfGuests);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
    }
}
