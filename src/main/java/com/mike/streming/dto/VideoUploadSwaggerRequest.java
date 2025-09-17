package com.mike.streming.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para la subida de videos optimizado para Swagger UI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para subir un video")
public class VideoUploadSwaggerRequest {

    @Schema(description = "Título del video", example = "Mi Video de Prueba", required = true)
    private String title;

    @Schema(description = "Descripción del video", example = "Este es un video de prueba para demostrar el sistema de streaming")
    private String description;

    @Schema(description = "Tags del video separados por comas", example = "tutorial,programacion,spring")
    private String tags;

    @Schema(description = "Indica si el video es público", example = "false", defaultValue = "false")
    private boolean isPublic = false;

    @Schema(description = "Indica si el video debe ser encriptado", example = "true", defaultValue = "false")
    private boolean encryptVideo = false;

    @Schema(description = "Archivo de video (formatos soportados: mp4, avi, mov, mkv)", 
            type = "string", format = "binary", required = true)
    private String file; // Este campo será manejado por Swagger como file upload

    /**
     * Convierte los tags de String a List
     */
    public List<String> getTagsAsList() {
        if (tags == null || tags.trim().isEmpty()) {
            return null;
        }
        return java.util.Arrays.asList(tags.split(","));
    }
}
