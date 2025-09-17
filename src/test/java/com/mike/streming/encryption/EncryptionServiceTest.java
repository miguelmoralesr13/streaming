package com.mike.streming.encryption;

import com.mike.streming.config.EncryptionConfig;
import com.mike.streming.exception.EncryptionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests para el servicio de encriptación
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EncryptionService Tests")
class EncryptionServiceTest {

    @Mock
    private EncryptionConfig encryptionConfig;

    @InjectMocks
    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        when(encryptionConfig.getAlgorithm()).thenReturn("AES");
        when(encryptionConfig.getKeySize()).thenReturn(256);
        when(encryptionConfig.getTransformation()).thenReturn("AES/CBC/PKCS5Padding");
        when(encryptionConfig.getMasterKey()).thenReturn("MyMasterKey123456789012345678901234567890");
    }

    @Test
    @DisplayName("Debería generar una clave de encriptación válida")
    void shouldGenerateValidEncryptionKey() {
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
    @DisplayName("Debería encriptar y desencriptar datos correctamente")
    void shouldEncryptAndDecryptDataCorrectly() {
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
        
        // Verificar que los datos encriptados son diferentes a los originales
        assertNotEquals(originalData, new String(encryptedData.getData()), "Los datos encriptados deben ser diferentes a los originales");
    }

    @Test
    @DisplayName("Debería encriptar y desencriptar strings correctamente")
    void shouldEncryptAndDecryptStringsCorrectly() {
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

    @Test
    @DisplayName("Debería encriptar y desencriptar claves con clave maestra")
    void shouldEncryptAndDecryptKeysWithMasterKey() {
        // Given
        String keyToEncrypt = "clave_secreta_123";

        // When
        String encryptedKey = encryptionService.encryptKey(keyToEncrypt);
        String decryptedKey = encryptionService.decryptKey(encryptedKey);

        // Then
        assertNotNull(encryptedKey, "La clave encriptada no debe ser null");
        assertNotNull(decryptedKey, "La clave desencriptada no debe ser null");
        assertEquals(keyToEncrypt, decryptedKey, "La clave desencriptada debe ser igual a la original");
        assertNotEquals(keyToEncrypt, encryptedKey, "La clave encriptada debe ser diferente a la original");
    }

    @Test
    @DisplayName("Debería manejar datos vacíos correctamente")
    void shouldHandleEmptyDataCorrectly() {
        // Given
        byte[] emptyData = new byte[0];
        String key = encryptionService.generateEncryptionKey();

        // When
        EncryptedData encryptedData = encryptionService.encrypt(emptyData, key);
        byte[] decryptedData = encryptionService.decrypt(encryptedData);

        // Then
        assertNotNull(encryptedData, "Los datos encriptados no deben ser null");
        assertNotNull(decryptedData, "Los datos desencriptados no deben ser null");
        assertArrayEquals(emptyData, decryptedData, "Los datos desencriptados deben ser iguales a los originales");
    }

    @Test
    @DisplayName("Debería manejar datos grandes correctamente")
    void shouldHandleLargeDataCorrectly() {
        // Given
        byte[] largeData = new byte[1024 * 1024]; // 1MB
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        String key = encryptionService.generateEncryptionKey();

        // When
        EncryptedData encryptedData = encryptionService.encrypt(largeData, key);
        byte[] decryptedData = encryptionService.decrypt(encryptedData);

        // Then
        assertNotNull(encryptedData, "Los datos encriptados no deben ser null");
        assertNotNull(decryptedData, "Los datos desencriptados no deben ser null");
        assertArrayEquals(largeData, decryptedData, "Los datos desencriptados deben ser iguales a los originales");
    }

    @Test
    @DisplayName("Debería fallar con clave inválida")
    void shouldFailWithInvalidKey() {
        // Given
        String invalidKey = "clave_invalida";
        String data = "datos de prueba";

        // When & Then
        assertThrows(EncryptionException.class, () -> {
            encryptionService.encrypt(data.getBytes(), invalidKey);
        }, "Debería lanzar EncryptionException con clave inválida");
    }

    @Test
    @DisplayName("Debería generar claves diferentes cada vez")
    void shouldGenerateDifferentKeysEachTime() {
        // When
        String key1 = encryptionService.generateEncryptionKey();
        String key2 = encryptionService.generateEncryptionKey();

        // Then
        assertNotEquals(key1, key2, "Las claves generadas deben ser diferentes");
    }

    @Test
    @DisplayName("Debería encriptar los mismos datos de manera diferente con IVs diferentes")
    void shouldEncryptSameDataDifferentlyWithDifferentIVs() {
        // Given
        String data = "mismo texto de prueba";
        String key = encryptionService.generateEncryptionKey();

        // When
        EncryptedData encrypted1 = encryptionService.encrypt(data.getBytes(), key);
        EncryptedData encrypted2 = encryptionService.encrypt(data.getBytes(), key);

        // Then
        assertNotNull(encrypted1, "Primera encriptación no debe ser null");
        assertNotNull(encrypted2, "Segunda encriptación no debe ser null");
        assertNotEquals(encrypted1.getData(), encrypted2.getData(), "Los datos encriptados deben ser diferentes debido a IVs diferentes");
        
        // Pero ambos deben desencriptar al mismo resultado
        String decrypted1 = new String(encryptionService.decrypt(encrypted1));
        String decrypted2 = new String(encryptionService.decrypt(encrypted2));
        assertEquals(data, decrypted1, "Primera desencriptación debe ser correcta");
        assertEquals(data, decrypted2, "Segunda desencriptación debe ser correcta");
    }
}
