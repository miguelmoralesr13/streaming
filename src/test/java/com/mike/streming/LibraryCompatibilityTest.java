package com.mike.streming;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para verificar la compatibilidad de las librerías de encriptación
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Library Compatibility Test")
class LibraryCompatibilityTest {

    @Test
    @DisplayName("Debería poder registrar BouncyCastle como proveedor de seguridad")
    void shouldRegisterBouncyCastleProvider() {
        // When
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        // Then
        assertNotNull(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME), 
                "BouncyCastle debe estar registrado como proveedor");
    }

    @Test
    @DisplayName("Debería soportar AES con BouncyCastle")
    void shouldSupportAESWithBouncyCastle() throws Exception {
        // Given
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        // When & Then
        assertDoesNotThrow(() -> {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", BouncyCastleProvider.PROVIDER_NAME);
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            assertNotNull(secretKey, "Debe poder generar claves AES con BouncyCastle");
        }, "Debe soportar generación de claves AES con BouncyCastle");
    }

    @Test
    @DisplayName("Debería soportar AES/CBC/PKCS5Padding con BouncyCastle")
    void shouldSupportAESCBCWithBouncyCastle() throws Exception {
        // Given
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        // When & Then
        assertDoesNotThrow(() -> {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", BouncyCastleProvider.PROVIDER_NAME);
            assertNotNull(cipher, "Debe poder crear instancia de Cipher AES/CBC/PKCS5Padding");
        }, "Debe soportar AES/CBC/PKCS5Padding con BouncyCastle");
    }

    @Test
    @DisplayName("Debería poder encriptar y desencriptar con AES/CBC/PKCS5Padding")
    void shouldEncryptDecryptWithAESCBC() throws Exception {
        // Given
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        String testData = "Datos de prueba para encriptación";
        byte[] testBytes = testData.getBytes();

        // Generar clave
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", BouncyCastleProvider.PROVIDER_NAME);
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();

        // Generar IV
        byte[] iv = new byte[16];
        java.security.SecureRandom.getInstanceStrong().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // When - Encriptar
        Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding", BouncyCastleProvider.PROVIDER_NAME);
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedData = encryptCipher.doFinal(testBytes);

        // When - Desencriptar
        Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding", BouncyCastleProvider.PROVIDER_NAME);
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] decryptedData = decryptCipher.doFinal(encryptedData);

        // Then
        assertNotNull(encryptedData, "Los datos encriptados no deben ser null");
        assertNotNull(decryptedData, "Los datos desencriptados no deben ser null");
        assertNotEquals(testData, new String(encryptedData), "Los datos encriptados deben ser diferentes");
        assertEquals(testData, new String(decryptedData), "Los datos desencriptados deben ser iguales a los originales");
    }

    @Test
    @DisplayName("Debería soportar el proveedor por defecto de Java para AES")
    void shouldSupportDefaultJavaProviderForAES() throws Exception {
        // When & Then
        assertDoesNotThrow(() -> {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            assertNotNull(secretKey, "Debe poder generar claves AES con proveedor por defecto");
        }, "Debe soportar generación de claves AES con proveedor por defecto");
    }

    @Test
    @DisplayName("Debería soportar AES/CBC/PKCS5Padding con proveedor por defecto de Java")
    void shouldSupportAESCBCWithDefaultProvider() throws Exception {
        // When & Then
        assertDoesNotThrow(() -> {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            assertNotNull(cipher, "Debe poder crear instancia de Cipher AES/CBC/PKCS5Padding con proveedor por defecto");
        }, "Debe soportar AES/CBC/PKCS5Padding con proveedor por defecto");
    }

    @Test
    @DisplayName("Debería poder encriptar y desencriptar con proveedor por defecto de Java")
    void shouldEncryptDecryptWithDefaultProvider() throws Exception {
        // Given
        String testData = "Datos de prueba para encriptación con proveedor por defecto";
        byte[] testBytes = testData.getBytes();

        // Generar clave
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();

        // Generar IV
        byte[] iv = new byte[16];
        java.security.SecureRandom.getInstanceStrong().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        // When - Encriptar
        Cipher encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedData = encryptCipher.doFinal(testBytes);

        // When - Desencriptar
        Cipher decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] decryptedData = decryptCipher.doFinal(encryptedData);

        // Then
        assertNotNull(encryptedData, "Los datos encriptados no deben ser null");
        assertNotNull(decryptedData, "Los datos desencriptados no deben ser null");
        assertNotEquals(testData, new String(encryptedData), "Los datos encriptados deben ser diferentes");
        assertEquals(testData, new String(decryptedData), "Los datos desencriptados deben ser iguales a los originales");
    }

    @Test
    @DisplayName("Debería verificar que las versiones de BouncyCastle son correctas")
    void shouldVerifyBouncyCastleVersions() {
        // When
        BouncyCastleProvider provider = new BouncyCastleProvider();
        String version = provider.getVersionStr();

        // Then
        assertNotNull(version, "La versión de BouncyCastle no debe ser null");
        assertTrue(version.contains("1.78"), "Debe ser versión 1.78.x de BouncyCastle");
        System.out.println("BouncyCastle version: " + version);
    }
}
