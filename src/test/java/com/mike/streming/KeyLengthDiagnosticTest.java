package com.mike.streming;

import com.mike.streming.encryption.EncryptionService;
import com.mike.streming.encryption.EncryptedData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para diagnosticar problemas de longitud de clave
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Key Length Diagnostic Test")
class KeyLengthDiagnosticTest {

    @Autowired
    private EncryptionService encryptionService;

    @Test
    @DisplayName("Debería diagnosticar la longitud de la clave generada")
    void shouldDiagnoseKeyLength() {
        // When
        String generatedKey = encryptionService.generateEncryptionKey();
        byte[] decodedKey = Base64.getDecoder().decode(generatedKey);
        
        // Then
        System.out.println("=== DIAGNÓSTICO DE CLAVE ===");
        System.out.println("Clave Base64 generada: " + generatedKey);
        System.out.println("Longitud de clave Base64: " + generatedKey.length());
        System.out.println("Longitud de clave decodificada: " + decodedKey.length + " bytes");
        System.out.println("Longitud esperada para AES-256: 32 bytes");
        
        // Verificar longitud
        assertEquals(32, decodedKey.length, "La clave debe tener exactamente 32 bytes para AES-256");
        
        // Verificar que la clave no sea null
        assertNotNull(decodedKey, "La clave decodificada no debe ser null");
        assertTrue(decodedKey.length > 0, "La clave debe tener contenido");
    }

    @Test
    @DisplayName("Debería encriptar con la clave generada correctamente")
    void shouldEncryptWithGeneratedKey() {
        // Given
        String testData = "Datos de prueba para encriptación";
        String generatedKey = encryptionService.generateEncryptionKey();
        
        System.out.println("=== PRUEBA DE ENCRIPTACIÓN ===");
        System.out.println("Datos originales: " + testData);
        System.out.println("Clave generada: " + generatedKey);
        
        // When
        EncryptedData encryptedData = encryptionService.encrypt(testData.getBytes(), generatedKey);
        
        // Then
        assertNotNull(encryptedData, "Los datos encriptados no deben ser null");
        assertNotNull(encryptedData.getData(), "Los datos encriptados deben tener contenido");
        assertNotNull(encryptedData.getKey(), "La clave no debe ser null");
        assertNotNull(encryptedData.getAlgorithm(), "El algoritmo no debe ser null");
        
        System.out.println("Encriptación exitosa!");
        System.out.println("Algoritmo usado: " + encryptedData.getAlgorithm());
        System.out.println("Tamaño de datos encriptados: " + encryptedData.getData().length + " bytes");
    }

    @Test
    @DisplayName("Debería verificar la conversión de la clave maestra a AES")
    void shouldVerifyMasterKeyConversion() {
        // Given
        String masterKey = "TestMasterKey123456789012345678901234567890";
        byte[] masterKeyBytes = masterKey.getBytes();
        
        System.out.println("=== DIAGNÓSTICO DE CLAVE MAESTRA ===");
        System.out.println("Clave maestra original: " + masterKey);
        System.out.println("Longitud de clave maestra original: " + masterKeyBytes.length + " bytes");
        System.out.println("Longitud esperada para AES-256: 32 bytes");
        
        // When - Crear clave AES a partir de la clave maestra
        String generatedKey = encryptionService.generateEncryptionKey();
        String encryptedKey = encryptionService.encryptKey(generatedKey);
        String decryptedKey = encryptionService.decryptKey(encryptedKey);
        
        System.out.println("Clave generada: " + generatedKey);
        System.out.println("Clave encriptada: " + encryptedKey);
        System.out.println("Clave desencriptada: " + decryptedKey);
        
        // Then
        assertEquals(generatedKey, decryptedKey, "La clave desencriptada debe ser igual a la original");
        System.out.println("✅ Conversión de clave maestra a AES funciona correctamente!");
    }
}
