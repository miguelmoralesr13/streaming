package com.mike.streming;

import com.mike.streming.encryption.EncryptionService;
import com.mike.streming.encryption.EncryptedData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple para verificar que la encriptación funciona
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Simple Encryption Test")
class SimpleEncryptionTest {

    @Autowired
    private EncryptionService encryptionService;

    @Test
    @DisplayName("Debería poder generar una clave de encriptación")
    void shouldGenerateEncryptionKey() {
        // When
        String key = encryptionService.generateEncryptionKey();

        // Then
        assertNotNull(key, "La clave no debe ser null");
        assertFalse(key.isEmpty(), "La clave no debe estar vacía");
        
        // Verificar que es Base64 válido
        assertDoesNotThrow(() -> {
            byte[] decoded = java.util.Base64.getDecoder().decode(key);
            assertEquals(32, decoded.length, "La clave debe tener 32 bytes (256 bits)");
        }, "La clave debe ser Base64 válido");
    }

    @Test
    @DisplayName("Debería poder encriptar y desencriptar datos")
    void shouldEncryptAndDecryptData() {
        // Given
        String originalData = "Este es un texto de prueba para encriptación";
        String key = encryptionService.generateEncryptionKey();

        // When
        EncryptedData encryptedData = encryptionService.encrypt(originalData.getBytes(), key);
        byte[] decryptedData = encryptionService.decrypt(encryptedData);

        // Then
        assertNotNull(encryptedData, "Los datos encriptados no deben ser null");
        assertNotNull(decryptedData, "Los datos desencriptados no deben ser null");
        assertEquals(originalData, new String(decryptedData), "Los datos desencriptados deben ser iguales a los originales");
    }

    @Test
    @DisplayName("Debería poder encriptar strings")
    void shouldEncryptStrings() {
        // Given
        String originalString = "String de prueba para encriptación";
        String key = encryptionService.generateEncryptionKey();

        // When
        EncryptedData encryptedData = encryptionService.encryptString(originalString, key);
        String decryptedString = encryptionService.decryptString(encryptedData);

        // Then
        assertNotNull(encryptedData, "Los datos encriptados no deben ser null");
        assertNotNull(decryptedString, "El string desencriptado no debe ser null");
        assertEquals(originalString, decryptedString, "El string desencriptado debe ser igual al original");
    }
}
