package com.mike.streming.security;

import com.mike.streming.config.EncryptionConfig;
import com.mike.streming.encryption.EncryptionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para verificar las políticas de encriptación
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Encryption Policy Tests")
class EncryptionPolicyTest {

    @Autowired
    private EncryptionConfig encryptionConfig;

    @Autowired
    private EncryptionService encryptionService;

    @Test
    @DisplayName("Debería tener configuración de encriptación válida")
    void shouldHaveValidEncryptionConfiguration() {
        // Then
        assertNotNull(encryptionConfig, "La configuración de encriptación no debe ser null");
        assertNotNull(encryptionConfig.getAlgorithm(), "El algoritmo no debe ser null");
        assertNotNull(encryptionConfig.getTransformation(), "La transformación no debe ser null");
        assertNotNull(encryptionConfig.getMasterKey(), "La clave maestra no debe ser null");
        
        assertEquals("AES", encryptionConfig.getAlgorithm(), "El algoritmo debe ser AES");
        assertEquals("AES/CBC/PKCS5Padding", encryptionConfig.getTransformation(), "La transformación debe ser AES/CBC/PKCS5Padding");
        assertEquals(256, encryptionConfig.getKeySize(), "El tamaño de clave debe ser 256 bits");
        
        assertFalse(encryptionConfig.getMasterKey().isEmpty(), "La clave maestra no debe estar vacía");
        assertTrue(encryptionConfig.getMasterKey().length() >= 32, "La clave maestra debe tener al menos 32 caracteres");
    }

    @Test
    @DisplayName("Debería poder generar claves de encriptación")
    void shouldBeAbleToGenerateEncryptionKeys() {
        // When
        String key1 = encryptionService.generateEncryptionKey();
        String key2 = encryptionService.generateEncryptionKey();

        // Then
        assertNotNull(key1, "La primera clave no debe ser null");
        assertNotNull(key2, "La segunda clave no debe ser null");
        assertFalse(key1.isEmpty(), "La primera clave no debe estar vacía");
        assertFalse(key2.isEmpty(), "La segunda clave no debe estar vacía");
        assertNotEquals(key1, key2, "Las claves deben ser diferentes");
        
        // Verificar que son Base64 válido
        assertDoesNotThrow(() -> {
            byte[] decoded1 = java.util.Base64.getDecoder().decode(key1);
            byte[] decoded2 = java.util.Base64.getDecoder().decode(key2);
            assertEquals(32, decoded1.length, "La primera clave debe tener 32 bytes");
            assertEquals(32, decoded2.length, "La segunda clave debe tener 32 bytes");
        }, "Las claves deben ser Base64 válido");
    }

    @Test
    @DisplayName("Debería poder encriptar y desencriptar datos")
    void shouldBeAbleToEncryptAndDecryptData() {
        // Given
        String testData = "Datos de prueba para encriptación";
        String key = encryptionService.generateEncryptionKey();

        // When
        var encryptedData = encryptionService.encrypt(testData.getBytes(), key);
        byte[] decryptedData = encryptionService.decrypt(encryptedData);

        // Then
        assertNotNull(encryptedData, "Los datos encriptados no deben ser null");
        assertNotNull(decryptedData, "Los datos desencriptados no deben ser null");
        assertEquals(testData, new String(decryptedData), "Los datos desencriptados deben ser iguales a los originales");
        
        // Verificar que los datos encriptados son diferentes a los originales
        assertNotEquals(testData, new String(encryptedData.getData()), "Los datos encriptados deben ser diferentes a los originales");
    }

    @Test
    @DisplayName("Debería poder encriptar claves con clave maestra")
    void shouldBeAbleToEncryptKeysWithMasterKey() {
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
    @DisplayName("Debería usar IVs diferentes para cada encriptación")
    void shouldUseDifferentIVsForEachEncryption() {
        // Given
        String testData = "Mismo texto de prueba";
        String key = encryptionService.generateEncryptionKey();

        // When
        var encrypted1 = encryptionService.encrypt(testData.getBytes(), key);
        var encrypted2 = encryptionService.encrypt(testData.getBytes(), key);

        // Then
        assertNotNull(encrypted1, "Primera encriptación no debe ser null");
        assertNotNull(encrypted2, "Segunda encriptación no debe ser null");
        assertNotEquals(encrypted1.getData(), encrypted2.getData(), "Los datos encriptados deben ser diferentes debido a IVs diferentes");
        
        // Pero ambos deben desencriptar al mismo resultado
        String decrypted1 = new String(encryptionService.decrypt(encrypted1));
        String decrypted2 = new String(encryptionService.decrypt(encrypted2));
        assertEquals(testData, decrypted1, "Primera desencriptación debe ser correcta");
        assertEquals(testData, decrypted2, "Segunda desencriptación debe ser correcta");
    }

    @Test
    @DisplayName("Debería manejar datos de diferentes tamaños")
    void shouldHandleDataOfDifferentSizes() {
        // Given
        String[] testDataArray = {
                "", // Datos vacíos
                "A", // 1 byte
                "Hello World", // 11 bytes
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", // 128 bytes
                new String(new byte[1024]) // 1KB
        };
        
        String key = encryptionService.generateEncryptionKey();

        for (String testData : testDataArray) {
            // When
            var encryptedData = encryptionService.encrypt(testData.getBytes(), key);
            byte[] decryptedData = encryptionService.decrypt(encryptedData);

            // Then
            assertNotNull(encryptedData, "Los datos encriptados no deben ser null para: " + testData.length() + " bytes");
            assertNotNull(decryptedData, "Los datos desencriptados no deben ser null para: " + testData.length() + " bytes");
            assertEquals(testData, new String(decryptedData), "Los datos desencriptados deben ser iguales a los originales para: " + testData.length() + " bytes");
        }
    }

    @Test
    @DisplayName("Debería tener configuración de seguridad adecuada")
    void shouldHaveAdequateSecurityConfiguration() {
        // Then
        // Verificar que el algoritmo es seguro
        assertTrue(encryptionConfig.getAlgorithm().equals("AES"), "Debe usar AES");
        assertTrue(encryptionConfig.getKeySize() >= 256, "El tamaño de clave debe ser al menos 256 bits");
        assertTrue(encryptionConfig.getTransformation().contains("AES"), "La transformación debe usar AES");
        assertTrue(encryptionConfig.getTransformation().contains("CBC"), "Debe usar modo CBC");
        assertTrue(encryptionConfig.getTransformation().contains("PKCS5Padding"), "Debe usar PKCS5Padding");
        
        // Verificar que la clave maestra es suficientemente larga
        assertTrue(encryptionConfig.getMasterKey().length() >= 32, "La clave maestra debe tener al menos 32 caracteres");
    }
}
