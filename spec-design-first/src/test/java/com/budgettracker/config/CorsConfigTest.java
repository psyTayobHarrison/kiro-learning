package com.budgettracker.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowCorsFromLocalhost4200() throws Exception {
        mockMvc.perform(options("/expenses")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void shouldAllowPostMethod() throws Exception {
        mockMvc.perform(options("/expenses")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "POST"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void shouldAllowPutMethod() throws Exception {
        mockMvc.perform(options("/expenses/1")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "PUT"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void shouldAllowDeleteMethod() throws Exception {
        mockMvc.perform(options("/expenses/1")
                .header("Origin", "http://localhost:4200")
                .header("Access-Control-Request-Method", "DELETE"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"));
    }

    @Test
    void shouldNotAllowCorsFromOtherOrigins() throws Exception {
        mockMvc.perform(options("/expenses")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET"))
            .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}
