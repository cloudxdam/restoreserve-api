package com.pachedev.restoreserve.controller;

import static org.hamcrest.Matchers.hasItem;
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

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:tablesdb")
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class RestaurantTableControllerTest {

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
    void listTablesAsUserReturns403() throws Exception {
        String token = loginAs("armymoves", "1234");

        mockMvc.perform(get("/api/v1/tables").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void listTablesAsAdminReturnsAllSeededTables() throws Exception {
        String token = loginAs("admin", "Admin1234");

        mockMvc.perform(get("/api/v1/tables").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[*].name", hasItem("Mesa 1")));
    }

    @Test
    void createTableAsUser() throws Exception {
        String token = loginAs("armymoves", "1234");

        mockMvc.perform(post("/api/v1/tables").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Mesa Nueva",
                                    "maxPax": 4,
                                    "location": "SALON"
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
