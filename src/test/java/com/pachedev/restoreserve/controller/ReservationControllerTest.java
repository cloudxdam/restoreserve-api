package com.pachedev.restoreserve.controller;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:reservationsdb")
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class ReservationControllerTest {

    private final MockMvc mockMvc;

    /**

     * Inicia sesión con las credenciales indicadas y obtiene el JWT de la respuesta.
     *
     * @param username nombre de usuario
     * @param password contraseña del usuario
     * @return JWT obtenido del endpoint de login
     * @throws Exception si la petición de login falla
     */
    private String loginAs(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "password": "%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void reservationWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userSeesOnlyOwnReservations() throws Exception {
        String token = loginAs("armymoves", "1234");

        mockMvc.perform(get("/api/v1/reservations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].customerName", everyItem(is("Armiche Santana"))));
    }

    @Test
    void userCannotReadOthersReservationReturns409() throws Exception {
        String token = loginAs("armymoves", "1234");

        mockMvc.perform(get("/api/v1/reservations/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_LOGIC_ERROR"));
    }

    @Test
    void reservationExceedingTableCapacityReturns409() throws Exception {
        String token = loginAs("armymoves", "1234");

        mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tableId": 1,
                                    "reservationDate": "2030-03-15T21:00:00",
                                    "numberOfGuests": 6,
                                    "isVip": false
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("BUSINESS_LOGIC_ERROR"));
    }

    @Test
    void nonVipUserCannotReserveVipTableReturns403() throws Exception {
        String token = loginAs("armymoves", "1234");

        mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tableId": 1,
                                    "reservationDate": "2030-04-20T19:00:00",
                                    "numberOfGuests": 2,
                                    "isVip": true
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("NOT_VIP_USER"));
    }

    @Test
    void pastReservationDateReturns400() throws Exception {
        String token = loginAs("armymoves", "1234");

        mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "tableId": 1,
                                    "reservationDate": "2020-01-01T20:00:00",
                                    "numberOfGuests": 2,
                                    "isVip": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.reservationDate").value("La reserva debe ser en una fecha futura"));
    }

    @Test
    void emptyReservationBodyReturns400() throws Exception {
        String token = loginAs("armymoves", "1234");

        mockMvc.perform(post("/api/v1/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.tableId").value("el id de la mesa es obligatorio"));
    }
}