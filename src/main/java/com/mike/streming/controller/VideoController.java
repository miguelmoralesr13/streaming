package com.mike.streming.controller;

import com.mike.streming.dto.VideoResponse;
import com.mike.streming.dto.VideoUploadRequest;
import com.mike.streming.dto.VideoUploadSwaggerRequest;
import com.mike.streming.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controlador para gestión de videos
 */
@Slf4j
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Videos", description = "API para gestión de videos")
@SecurityRequirement(name = "bearerAuth")
public class VideoController {
    
    private final VideoService videoService;
    
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Subir video", 
        description = "Sube un nuevo video al sistema con metadata. " +
                    "Formatos soportados: MP4, AVI, MOV, MKV. " +
                    "Tamaño máximo: 2GB.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(implementation = VideoUploadSwaggerRequest.class)
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "201", 
                description = "Video subido exitosamente",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = VideoResponse.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o archivo no soportado"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "413", description = "Archivo demasiado grande")
    })
    public ResponseEntity<VideoResponse> uploadVideo(
            @Parameter(description = "Archivo de video (MP4, AVI, MOV, MKV)", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Título del video", required = true, example = "Mi Video de Prueba")
            @RequestParam("title") String title,
            
            @Parameter(description = "Descripción del video", example = "Este es un video de prueba")
            @RequestParam(value = "description", required = false) String description,
            
            @Parameter(description = "Tags separados por comas", example = "tutorial,programacion,spring")
            @RequestParam(value = "tags", required = false) String tags,
            
            @Parameter(description = "Indica si el video es público", example = "false")
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
            
            @Parameter(description = "Indica si el video debe ser encriptado", example = "false")
            @RequestParam(value = "encryptVideo", defaultValue = "false") boolean encryptVideo) {
        
        log.info("Video upload request: {} - Title: {} - Public: {} - Encrypted: {}", 
                file.getOriginalFilename(), title, isPublic, encryptVideo);
        
        // Validar archivo
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }
        
        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("El archivo debe ser un video válido");
        }
        
        VideoUploadRequest request = VideoUploadRequest.builder()
                .title(title)
                .description(description)
                .tags(tags != null ? java.util.Arrays.asList(tags.split(",")) : null)
                .isPublic(isPublic)
                .encryptVideo(encryptVideo)
                .build();
        
        VideoResponse response = videoService.uploadVideo(file, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping(value = "/upload-swagger", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Subir video (Swagger UI)", 
        description = "Endpoint optimizado para Swagger UI. Permite subir videos con metadata usando un formulario multipart.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                schema = @Schema(
                    type = "object",
                    requiredProperties = {"file", "title"},
                    example = "{\"file\": \"[binary data]\", \"title\": \"Mi Video\", \"description\": \"Descripción\", \"tags\": \"tag1,tag2\", \"isPublic\": false, \"encryptVideo\": false}"
                )
            )
        )
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "201", 
                description = "Video subido exitosamente",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = VideoResponse.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<VideoResponse> uploadVideoSwagger(
            @Parameter(description = "Archivo de video", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Título del video", required = true)
            @RequestParam("title") String title,
            
            @Parameter(description = "Descripción del video")
            @RequestParam(value = "description", required = false) String description,
            
            @Parameter(description = "Tags separados por comas")
            @RequestParam(value = "tags", required = false) String tags,
            
            @Parameter(description = "Video público")
            @RequestParam(value = "isPublic", defaultValue = "false") boolean isPublic,
            
            @Parameter(description = "Encriptar video")
            @RequestParam(value = "encryptVideo", defaultValue = "false") boolean encryptVideo) {
        
        log.info("Swagger video upload request: {} - Title: {}", file.getOriginalFilename(), title);
        
        VideoUploadRequest request = VideoUploadRequest.builder()
                .title(title)
                .description(description)
                .tags(tags != null ? java.util.Arrays.asList(tags.split(",")) : null)
                .isPublic(isPublic)
                .encryptVideo(encryptVideo)
                .build();
        
        VideoResponse response = videoService.uploadVideo(file, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{videoId}")
    @Operation(summary = "Obtener video", description = "Obtiene la información de un video específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video encontrado"),
            @ApiResponse(responseCode = "404", description = "Video no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<VideoResponse> getVideo(
            @Parameter(description = "ID del video") @PathVariable String videoId) {
        
        VideoResponse response = videoService.getVideoById(videoId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my-videos")
    @Operation(summary = "Mis videos", description = "Obtiene los videos del usuario autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Videos obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    public ResponseEntity<Page<VideoResponse>> getMyVideos(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<VideoResponse> videos = videoService.getUserVideos(pageable);
        return ResponseEntity.ok(videos);
    }
    
    @GetMapping("/public")
    @Operation(summary = "Videos públicos", description = "Obtiene videos públicos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Videos obtenidos exitosamente")
    })
    public ResponseEntity<Page<VideoResponse>> getPublicVideos(
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<VideoResponse> videos = videoService.getPublicVideos(pageable);
        return ResponseEntity.ok(videos);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Buscar videos", description = "Busca videos por título")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda completada")
    })
    public ResponseEntity<Page<VideoResponse>> searchVideos(
            @Parameter(description = "Término de búsqueda") @RequestParam String title,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<VideoResponse> videos = videoService.searchVideosByTitle(title, pageable);
        return ResponseEntity.ok(videos);
    }
    
    @PutMapping("/{videoId}")
    @Operation(summary = "Actualizar video", description = "Actualiza la metadata de un video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Video no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<VideoResponse> updateVideo(
            @Parameter(description = "ID del video") @PathVariable String videoId,
            @Valid @RequestBody VideoUploadRequest request) {
        
        VideoResponse response = videoService.updateVideo(videoId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{videoId}")
    @Operation(summary = "Eliminar video", description = "Elimina un video del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Video no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<Void> deleteVideo(
            @Parameter(description = "ID del video") @PathVariable String videoId) {
        
        videoService.deleteVideo(videoId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{videoId}/view")
    @Operation(summary = "Registrar vista", description = "Incrementa el contador de vistas de un video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vista registrada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Video no encontrado")
    })
    public ResponseEntity<Void> incrementViewCount(
            @Parameter(description = "ID del video") @PathVariable String videoId) {
        
        videoService.incrementViewCount(videoId);
        return ResponseEntity.ok().build();
    }
}
