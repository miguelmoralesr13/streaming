package com.mike.streming.encryption;

import com.mike.streming.config.EncryptionConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests para el servicio de encriptación de videos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VideoEncryptionService Tests")
class VideoEncryptionServiceTest {

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private EncryptionConfig encryptionConfig;

    @InjectMocks
    private VideoEncryptionService videoEncryptionService;

    @BeforeEach
    void setUp() {
        when(encryptionConfig.getTransformation()).thenReturn("AES/CBC/PKCS5Padding");
    }

    @Test
    @DisplayName("Debería encriptar un video completo correctamente")
    void shouldEncryptCompleteVideoCorrectly() throws Exception {
        // Given
        String videoContent = "Contenido de video de prueba para encriptación";
        InputStream videoStream = new ByteArrayInputStream(videoContent.getBytes());
        String encryptionKey = "clave_de_encriptacion_123";
        
        EncryptedData mockEncryptedData = EncryptedData.builder()
                .data("datos_encriptados".getBytes())
                .key(encryptionKey)
                .algorithm("AES/CBC/PKCS5Padding")
                .build();

        when(encryptionService.encrypt(videoContent.getBytes(), encryptionKey))
                .thenReturn(mockEncryptedData);

        // When
        CompletableFuture<EncryptedData> future = videoEncryptionService.encryptVideo(videoStream, encryptionKey);
        EncryptedData result = future.get(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(result, "El resultado no debe ser null");
        assertEquals(encryptionKey, result.getKey(), "La clave debe ser la misma");
        assertEquals("AES/CBC/PKCS5Padding", result.getAlgorithm(), "El algoritmo debe ser correcto");
        assertNotNull(result.getData(), "Los datos encriptados no deben ser null");
    }

    @Test
    @DisplayName("Debería desencriptar un video completo correctamente")
    void shouldDecryptCompleteVideoCorrectly() throws Exception {
        // Given
        String originalContent = "Contenido original del video";
        String encryptionKey = "clave_de_encriptacion_123";
        
        EncryptedData encryptedData = EncryptedData.builder()
                .data("datos_encriptados".getBytes())
                .key(encryptionKey)
                .algorithm("AES/CBC/PKCS5Padding")
                .build();

        when(encryptionService.decrypt(encryptedData))
                .thenReturn(originalContent.getBytes());

        // When
        CompletableFuture<byte[]> future = videoEncryptionService.decryptVideo(encryptedData);
        byte[] result = future.get(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(result, "El resultado no debe ser null");
        assertEquals(originalContent, new String(result), "El contenido desencriptado debe ser correcto");
    }

    @Test
    @DisplayName("Debería manejar videos grandes correctamente")
    void shouldHandleLargeVideosCorrectly() throws Exception {
        // Given
        byte[] largeVideoData = new byte[5 * 1024 * 1024]; // 5MB
        for (int i = 0; i < largeVideoData.length; i++) {
            largeVideoData[i] = (byte) (i % 256);
        }
        
        InputStream videoStream = new ByteArrayInputStream(largeVideoData);
        String encryptionKey = "clave_de_encriptacion_123";
        
        EncryptedData mockEncryptedData = EncryptedData.builder()
                .data("datos_encriptados_grandes".getBytes())
                .key(encryptionKey)
                .algorithm("AES/CBC/PKCS5Padding")
                .build();

        when(encryptionService.encrypt(largeVideoData, encryptionKey))
                .thenReturn(mockEncryptedData);

        // When
        CompletableFuture<EncryptedData> future = videoEncryptionService.encryptVideo(videoStream, encryptionKey);
        EncryptedData result = future.get(10, TimeUnit.SECONDS);

        // Then
        assertNotNull(result, "El resultado no debe ser null");
        assertEquals(encryptionKey, result.getKey(), "La clave debe ser la misma");
        assertNotNull(result.getData(), "Los datos encriptados no deben ser null");
    }

    @Test
    @DisplayName("Debería manejar streams vacíos correctamente")
    void shouldHandleEmptyStreamsCorrectly() throws Exception {
        // Given
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        String encryptionKey = "clave_de_encriptacion_123";
        
        EncryptedData mockEncryptedData = EncryptedData.builder()
                .data(new byte[0])
                .key(encryptionKey)
                .algorithm("AES/CBC/PKCS5Padding")
                .build();

        when(encryptionService.encrypt(new byte[0], encryptionKey))
                .thenReturn(mockEncryptedData);

        // When
        CompletableFuture<EncryptedData> future = videoEncryptionService.encryptVideo(emptyStream, encryptionKey);
        EncryptedData result = future.get(5, TimeUnit.SECONDS);

        // Then
        assertNotNull(result, "El resultado no debe ser null");
        assertEquals(encryptionKey, result.getKey(), "La clave debe ser la misma");
        assertNotNull(result.getData(), "Los datos encriptados no deben ser null");
    }

    @Test
    @DisplayName("Debería manejar errores de encriptación correctamente")
    void shouldHandleEncryptionErrorsCorrectly() {
        // Given
        String videoContent = "Contenido de video de prueba";
        InputStream videoStream = new ByteArrayInputStream(videoContent.getBytes());
        String encryptionKey = "clave_invalida";

        when(encryptionService.encrypt(videoContent.getBytes(), encryptionKey))
                .thenThrow(new RuntimeException("Error de encriptación"));

        // When & Then
        CompletableFuture<EncryptedData> future = videoEncryptionService.encryptVideo(videoStream, encryptionKey);
        
        assertThrows(Exception.class, () -> {
            future.get(5, TimeUnit.SECONDS);
        }, "Debería lanzar excepción cuando hay error de encriptación");
    }

    @Test
    @DisplayName("Debería manejar errores de desencriptación correctamente")
    void shouldHandleDecryptionErrorsCorrectly() {
        // Given
        EncryptedData invalidEncryptedData = EncryptedData.builder()
                .data("datos_invalidos".getBytes())
                .key("clave_invalida")
                .algorithm("AES/CBC/PKCS5Padding")
                .build();

        when(encryptionService.decrypt(invalidEncryptedData))
                .thenThrow(new RuntimeException("Error de desencriptación"));

        // When & Then
        CompletableFuture<byte[]> future = videoEncryptionService.decryptVideo(invalidEncryptedData);
        
        assertThrows(Exception.class, () -> {
            future.get(5, TimeUnit.SECONDS);
        }, "Debería lanzar excepción cuando hay error de desencriptación");
    }
}
