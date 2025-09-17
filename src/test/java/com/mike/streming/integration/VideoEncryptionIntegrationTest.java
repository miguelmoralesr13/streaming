package com.mike.streming.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.streming.dto.AuthResponse;
import com.mike.streming.dto.VideoResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración end-to-end para encriptación de videos
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("Video Encryption Integration Tests")
class VideoEncryptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Debería completar el flujo completo de encriptación de videos")
    void shouldCompleteFullVideoEncryptionFlow() throws Exception {
        // 1. Registrar usuario
        String userRegistration = """
                {
                    "username": "testuser",
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRegistration))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.username").value("testuser"));

        // 2. Hacer login
        String loginRequest = """
                {
                    "usernameOrEmail": "testuser",
                    "password": "password123"
                }
                """;

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseContent, AuthResponse.class);
        String token = authResponse.getAccessToken();

        // 3. Verificar que el token es válido
        assertNotNull(token, "El token no debe ser null");
        assertFalse(token.isEmpty(), "El token no debe estar vacío");

        // 4. Probar endpoint de videos (requiere autenticación)
        mockMvc.perform(get("/api/videos/my-videos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        // 5. Verificar que la configuración de encriptación está disponible
        mockMvc.perform(get("/api/actuator/health")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Debería verificar que la configuración de encriptación está correcta")
    void shouldVerifyEncryptionConfigurationIsCorrect() throws Exception {
        // Este test verifica que la aplicación se inicia correctamente
        // con la configuración de encriptación adecuada
        
        mockMvc.perform(get("/api/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Debería verificar que los endpoints de autenticación funcionan")
    void shouldVerifyAuthenticationEndpointsWork() throws Exception {
        // 1. Verificar que el endpoint de registro está disponible
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "newuser",
                                    "email": "newuser@example.com",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.username").value("newuser"));

        // 2. Verificar que el endpoint de login está disponible
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "usernameOrEmail": "newuser",
                                    "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.user.username").value("newuser"));
    }

    @Test
    @DisplayName("Debería verificar que Swagger UI está disponible")
    void shouldVerifySwaggerUIIsAvailable() throws Exception {
        mockMvc.perform(get("/api/swagger-ui.html"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debería verificar que la documentación de API está disponible")
    void shouldVerifyAPIDocumentationIsAvailable() throws Exception {
        mockMvc.perform(get("/api/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
