package com.mike.streming.encryption;

import com.mike.streming.config.EncryptionConfig;
import com.mike.streming.exception.EncryptionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

/**
 * Servicio de encriptación AES-256-CBC
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EncryptionService {
    
    private final EncryptionConfig encryptionConfig;
    private static final int IV_LENGTH = 16; // Para AES/CBC
    
    /**
     * Generar clave de encriptación aleatoria
     */
    public String generateEncryptionKey() {
        try {
            // Usar el proveedor por defecto de Java para AES
            KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionConfig.getAlgorithm());
            keyGenerator.init(encryptionConfig.getKeySize());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            log.error("Error generating encryption key: {}", e.getMessage());
            throw new EncryptionException("Failed to generate encryption key", e);
        }
    }
    
    /**
     * Encriptar datos
     */
    public EncryptedData encrypt(byte[] data, String base64Key) {
        try {
            SecretKey secretKey = new SecretKeySpec(
                    Base64.getDecoder().decode(base64Key), 
                    encryptionConfig.getAlgorithm()
            );
            
            // Usar el proveedor por defecto de Java para AES/CBC/PKCS5Padding
            Cipher cipher = Cipher.getInstance(encryptionConfig.getTransformation());
            
            // Generar IV aleatorio
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            byte[] encryptedData = cipher.doFinal(data);
            
            // Combinar IV + datos encriptados
            byte[] encryptedWithIv = new byte[IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, IV_LENGTH);
            System.arraycopy(encryptedData, 0, encryptedWithIv, IV_LENGTH, encryptedData.length);
            
            return EncryptedData.builder()
                    .data(encryptedWithIv)
                    .key(base64Key)
                    .algorithm(encryptionConfig.getTransformation())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error encrypting data: {}", e.getMessage());
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }
    
    /**
     * Desencriptar datos
     */
    public byte[] decrypt(EncryptedData encryptedData) {
        try {
            SecretKey secretKey = new SecretKeySpec(
                    Base64.getDecoder().decode(encryptedData.getKey()), 
                    encryptionConfig.getAlgorithm()
            );
            
            // Usar el proveedor por defecto de Java para AES/CBC/PKCS5Padding
            Cipher cipher = Cipher.getInstance(encryptedData.getAlgorithm());
            
            // Extraer IV
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(encryptedData.getData(), 0, iv, 0, IV_LENGTH);
            
            // Extraer datos encriptados
            byte[] encryptedBytes = new byte[encryptedData.getData().length - IV_LENGTH];
            System.arraycopy(encryptedData.getData(), IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);
            
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            return cipher.doFinal(encryptedBytes);
            
        } catch (Exception e) {
            log.error("Error decrypting data: {}", e.getMessage());
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }
    
    /**
     * Encriptar string
     */
    public EncryptedData encryptString(String data, String base64Key) {
        return encrypt(data.getBytes(StandardCharsets.UTF_8), base64Key);
    }
    
    /**
     * Desencriptar string
     */
    public String decryptString(EncryptedData encryptedData) {
        byte[] decryptedBytes = decrypt(encryptedData);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
    
    /**
     * Encriptar clave con clave maestra
     */
    public String encryptKey(String keyToEncrypt) {
        try {
            // Crear una clave AES válida a partir de la clave maestra
            String masterKeyBase64 = createAESKeyFromMasterKey(encryptionConfig.getMasterKey());
            
            byte[] keyBytes = keyToEncrypt.getBytes(StandardCharsets.UTF_8);
            EncryptedData encrypted = encrypt(keyBytes, masterKeyBase64);
            return Base64.getEncoder().encodeToString(encrypted.getData());
        } catch (Exception e) {
            log.error("Error encrypting key: {}", e.getMessage());
            throw new EncryptionException("Failed to encrypt key", e);
        }
    }
    
    /**
     * Desencriptar clave con clave maestra
     */
    public String decryptKey(String encryptedKey) {
        try {
            // Crear una clave AES válida a partir de la clave maestra
            String masterKeyBase64 = createAESKeyFromMasterKey(encryptionConfig.getMasterKey());
            
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedKey);
            EncryptedData encryptedData = EncryptedData.builder()
                    .data(encryptedBytes)
                    .key(masterKeyBase64)
                    .algorithm(encryptionConfig.getTransformation())
                    .build();
            
            byte[] decryptedBytes = decrypt(encryptedData);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error decrypting key: {}", e.getMessage());
            throw new EncryptionException("Failed to decrypt key", e);
        }
    }
    
    /**
     * Crear una clave AES válida a partir de la clave maestra
     */
    private String createAESKeyFromMasterKey(String masterKey) {
        try {
            // Usar SHA-256 para crear una clave de 32 bytes a partir de la clave maestra
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(masterKey.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Error creating AES key from master key: {}", e.getMessage());
            throw new EncryptionException("Failed to create AES key from master key", e);
        }
    }
}
