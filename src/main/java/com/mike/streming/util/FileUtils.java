package com.mike.streming.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * Utilidades para manejo de archivos
 */
@Slf4j
public class FileUtils {
    
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4",
            "video/avi",
            "video/mov",
            "video/wmv",
            "video/flv",
            "video/webm",
            "video/mkv",
            "video/3gp"
    );
    
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB
    
    /**
     * Validar tipo de archivo de video
     */
    public static boolean isValidVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase());
    }
    
    /**
     * Validar tamaño de archivo
     */
    public static boolean isValidFileSize(MultipartFile file) {
        return file != null && file.getSize() <= MAX_FILE_SIZE;
    }
    
    /**
     * Obtener extensión del archivo
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    /**
     * Generar nombre de archivo único
     */
    public static String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf((int) (Math.random() * 1000));
        
        return timestamp + "_" + random + (extension.isEmpty() ? "" : "." + extension);
    }
    
    /**
     * Formatear tamaño de archivo
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Validar archivo completo
     */
    public static void validateVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }
        
        if (!isValidVideoFile(file)) {
            throw new IllegalArgumentException("Invalid video file type. Allowed types: " + ALLOWED_VIDEO_TYPES);
        }
        
        if (!isValidFileSize(file)) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + formatFileSize(MAX_FILE_SIZE));
        }
    }
}
