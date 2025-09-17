package com.mike.streming.encryption;

import com.mike.streming.exception.EncryptionException;
import com.mike.streming.model.Video;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio especializado para encriptación de videos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoEncryptionService {
    
    private final EncryptionService encryptionService;
    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB chunks
    
    /**
     * Encriptar video completo
     */
    public CompletableFuture<EncryptedData> encryptVideo(InputStream videoStream, String encryptionKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Starting video encryption");
                
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[CHUNK_SIZE];
                int bytesRead;
                
                while ((bytesRead = videoStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                byte[] videoData = outputStream.toByteArray();
                log.info("Video data loaded, size: {} bytes", videoData.length);
                
                EncryptedData encryptedData = encryptionService.encrypt(videoData, encryptionKey);
                log.info("Video encryption completed");
                
                return encryptedData;
                
            } catch (IOException e) {
                log.error("Error reading video stream: {}", e.getMessage());
                throw new EncryptionException("Failed to read video stream", e);
            } catch (Exception e) {
                log.error("Error encrypting video: {}", e.getMessage());
                throw new EncryptionException("Failed to encrypt video", e);
            }
        });
    }
    
    /**
     * Desencriptar video completo
     */
    public CompletableFuture<byte[]> decryptVideo(EncryptedData encryptedData) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Starting video decryption");
                byte[] decryptedData = encryptionService.decrypt(encryptedData);
                log.info("Video decryption completed, size: {} bytes", decryptedData.length);
                return decryptedData;
            } catch (Exception e) {
                log.error("Error decrypting video: {}", e.getMessage());
                throw new EncryptionException("Failed to decrypt video", e);
            }
        });
    }
    
    /**
     * Encriptar video en streaming (chunks)
     */
    public InputStream encryptVideoStream(InputStream videoStream, String encryptionKey) {
        try {
            log.info("Starting streaming video encryption");
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            
            while ((bytesRead = videoStream.read(buffer)) != -1) {
                // Encriptar chunk
                byte[] chunk = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunk, 0, bytesRead);
                
                EncryptedData encryptedChunk = encryptionService.encrypt(chunk, encryptionKey);
                outputStream.write(encryptedChunk.getData());
            }
            
            byte[] encryptedData = outputStream.toByteArray();
            log.info("Streaming video encryption completed, size: {} bytes", encryptedData.length);
            
            return new ByteArrayInputStream(encryptedData);
            
        } catch (IOException e) {
            log.error("Error in streaming video encryption: {}", e.getMessage());
            throw new EncryptionException("Failed to encrypt video stream", e);
        }
    }
    
    /**
     * Desencriptar video en streaming (chunks)
     */
    public InputStream decryptVideoStream(InputStream encryptedStream, String encryptionKey) {
        try {
            log.info("Starting streaming video decryption");
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            
            while ((bytesRead = encryptedStream.read(buffer)) != -1) {
                // Desencriptar chunk
                EncryptedData encryptedChunk = EncryptedData.builder()
                        .data(buffer)
                        .key(encryptionKey)
                        .algorithm("AES/CBC/PKCS5Padding")
                        .build();
                
                byte[] decryptedChunk = encryptionService.decrypt(encryptedChunk);
                outputStream.write(decryptedChunk);
            }
            
            byte[] decryptedData = outputStream.toByteArray();
            log.info("Streaming video decryption completed, size: {} bytes", decryptedData.length);
            
            return new ByteArrayInputStream(decryptedData);
            
        } catch (IOException e) {
            log.error("Error in streaming video decryption: {}", e.getMessage());
            throw new EncryptionException("Failed to decrypt video stream", e);
        }
    }
    
    /**
     * Generar clave de encriptación para video
     */
    public String generateVideoEncryptionKey() {
        return encryptionService.generateEncryptionKey();
    }
    
    /**
     * Encriptar clave de video con clave maestra
     */
    public String encryptVideoKey(String videoKey) {
        return encryptionService.encryptKey(videoKey);
    }
    
    /**
     * Desencriptar clave de video con clave maestra
     */
    public String decryptVideoKey(String encryptedVideoKey) {
        return encryptionService.decryptKey(encryptedVideoKey);
    }
    
    /**
     * Verificar si un video está encriptado
     */
    public boolean isVideoEncrypted(Video video) {
        return video.isEncrypted() && video.getEncryptionKey() != null && !video.getEncryptionKey().isEmpty();
    }
}
