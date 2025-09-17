package com.mike.streming.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import jakarta.servlet.MultipartConfigElement;

/**
 * Configuración para manejo de archivos grandes
 */
@Configuration
public class FileUploadConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        
        // Límite de archivo individual: 2GB
        factory.setMaxFileSize(DataSize.ofGigabytes(2));
        
        // Límite total de la petición: 2GB
        factory.setMaxRequestSize(DataSize.ofGigabytes(2));
        
        // Umbral para escribir a disco: 2KB
        factory.setFileSizeThreshold(DataSize.ofKilobytes(2));
        
        // Ubicación temporal para archivos
        factory.setLocation(System.getProperty("java.io.tmpdir"));
        
        return factory.createMultipartConfig();
    }
}
