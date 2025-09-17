package com.mike.streming.controller;

import com.mike.streming.encryption.VideoEncryptionService;
import com.mike.streming.exception.ResourceNotFoundException;
import com.mike.streming.exception.ValidationException;
import com.mike.streming.model.Video;
import com.mike.streming.repository.VideoRepository;
import com.mike.streming.service.GridFsService;
import com.mike.streming.service.VideoService;
import com.mike.streming.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

/**
 * Controlador para streaming de videos
 */
@Slf4j
@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
@Tag(name = "Video Streaming", description = "API para streaming de videos")
@SecurityRequirement(name = "bearerAuth")
public class StreamingController {
    
    private final VideoRepository videoRepository;
    private final GridFsService gridFsService;
    private final VideoEncryptionService videoEncryptionService;
    private final VideoService videoService;
    
    @GetMapping("/{videoId}/stream")
    @Operation(summary = "Stream de video", description = "Stream de video con soporte para HTTP Range Requests")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stream exitoso"),
            @ApiResponse(responseCode = "206", description = "Partial content"),
            @ApiResponse(responseCode = "404", description = "Video no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public void streamVideo(
            @Parameter(description = "ID del video") @PathVariable String videoId,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.info("Stream request for video: {}", videoId);
        
        // Obtener video
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));
        
        // Verificar permisos
        if (!video.isPublic() && !SecurityUtils.canAccessResource(video.getUploadedBy())) {
            throw new ValidationException("Access denied to this video");
        }
        
        // Verificar que el video esté listo
        if (video.getStatus() != Video.VideoStatus.READY) {
            throw new ValidationException("Video is not ready for streaming");
        }
        
        try {
            // Obtener archivo de GridFS
            GridFsResource resource = gridFsService.getFile(video.getGridfsFileId());
            
            // Procesar Range Request
            String rangeHeader = request.getHeader("Range");
            if (rangeHeader != null) {
                streamWithRange(resource, response, rangeHeader, video);
            } else {
                streamFullVideo(resource, response, video);
            }
            
            // Incrementar contador de vistas
            videoService.incrementViewCount(videoId);
            
        } catch (ResourceNotFoundException e) {
            log.error("File not found for video {}: {}", videoId, e.getMessage());
            
            // Ejecutar diagnóstico para ayudar con el debugging
            log.info("Ejecutando diagnóstico para video {} con fileId: {}", videoId, video.getGridfsFileId());
            gridFsService.diagnoseFile(video.getGridfsFileId());
            
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Video file not found\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
    
    @GetMapping("/{videoId}/diagnose")
    @Operation(summary = "Diagnosticar archivo de video", description = "Ejecuta diagnóstico completo del archivo en GridFS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Diagnóstico completado"),
            @ApiResponse(responseCode = "404", description = "Video no encontrado")
    })
    public ResponseEntity<String> diagnoseVideo(
            @Parameter(description = "ID del video") @PathVariable String videoId) {
        
        log.info("Diagnostic request for video: {}", videoId);
        
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));
        
        log.info("Video encontrado: {} - FileId: {}", video.getTitle(), video.getGridfsFileId());
        
        // Ejecutar diagnóstico
        gridFsService.diagnoseFile(video.getGridfsFileId());
        
        return ResponseEntity.ok("Diagnóstico completado. Revisa los logs para más detalles.");
    }
    
    @GetMapping("/{videoId}/thumbnail")
    @Operation(summary = "Thumbnail del video", description = "Obtiene el thumbnail de un video")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thumbnail obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Thumbnail no encontrado")
    })
    public ResponseEntity<byte[]> getThumbnail(
            @Parameter(description = "ID del video") @PathVariable String videoId) {
        
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));
        
        if (video.getThumbnailId() == null) {
            throw new ResourceNotFoundException("Thumbnail not found for video: " + videoId);
        }
        
        try {
            GridFsResource resource = gridFsService.getFile(video.getThumbnailId());
            byte[] thumbnailData = resource.getInputStream().readAllBytes();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(thumbnailData);
                    
        } catch (IOException e) {
            log.error("Error reading thumbnail: {}", e.getMessage());
            throw new ResourceNotFoundException("Failed to read thumbnail");
        }
    }
    
    @GetMapping("/{videoId}/download")
    @Operation(summary = "Descargar video", description = "Descarga el video completo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Descarga exitosa"),
            @ApiResponse(responseCode = "404", description = "Video no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public void downloadVideo(
            @Parameter(description = "ID del video") @PathVariable String videoId,
            HttpServletResponse response) throws IOException {
        
        log.info("Download request for video: {}", videoId);
        
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));
        
        // Verificar permisos
        if (!SecurityUtils.canAccessResource(video.getUploadedBy())) {
            throw new ValidationException("Access denied to download this video");
        }
        
        GridFsResource resource = gridFsService.getFile(video.getGridfsFileId());
        
        // Configurar headers para descarga
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", 
                "attachment; filename=\"" + video.getOriginalFilename() + "\"");
        response.setContentLengthLong(resource.contentLength());
        
        // Stream del archivo
        try (InputStream inputStream = resource.getInputStream();
             OutputStream outputStream = response.getOutputStream()) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
    }
    
    /**
     * Stream con soporte para Range Requests
     */
    private void streamWithRange(GridFsResource resource, HttpServletResponse response, 
                                String rangeHeader, Video video) throws IOException {
        
        long fileSize = resource.contentLength();
        long start = 0;
        long end = fileSize - 1;
        
        // Parsear Range header
        if (rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            start = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }
        }
        
        long contentLength = end - start + 1;
        
        // Configurar headers
        response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
        response.setHeader("Content-Range", String.format("bytes %d-%d/%d", start, end, fileSize));
        response.setHeader("Accept-Ranges", "bytes");
        response.setContentLengthLong(contentLength);
        response.setContentType(video.getContentType());
        
        // Stream del rango específico
        try (InputStream inputStream = resource.getInputStream();
             OutputStream outputStream = response.getOutputStream()) {
            
            // Saltar al inicio del rango
            inputStream.skip(start);
            
            byte[] buffer = new byte[8192];
            long remaining = contentLength;
            
            while (remaining > 0) {
                int bytesToRead = (int) Math.min(buffer.length, remaining);
                int bytesRead = inputStream.read(buffer, 0, bytesToRead);
                
                if (bytesRead == -1) break;
                
                outputStream.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
            
            outputStream.flush();
        }
    }
    
    /**
     * Stream del video completo
     */
    private void streamFullVideo(GridFsResource resource, HttpServletResponse response, Video video) throws IOException {
        
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(video.getContentType());
        response.setContentLengthLong(resource.contentLength());
        response.setHeader("Accept-Ranges", "bytes");
        
        try (InputStream inputStream = resource.getInputStream();
             OutputStream outputStream = response.getOutputStream()) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
    }
    
    @GetMapping("/{videoId}/info")
    @Operation(summary = "Información del video", description = "Obtiene información básica del video para streaming progresivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Video no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<VideoStreamInfo> getVideoInfo(
            @Parameter(description = "ID del video") @PathVariable String videoId) {
        
        log.info("Video info request for video: {}", videoId);
        
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));
        
        // Verificar permisos
        if (!video.isPublic() && !SecurityUtils.canAccessResource(video.getUploadedBy())) {
            throw new ValidationException("Access denied to this video");
        }
        
        // Verificar que el video esté listo
        if (video.getStatus() != Video.VideoStatus.READY) {
            throw new ValidationException("Video is not ready for streaming");
        }
        
        try {
            GridFsResource resource = gridFsService.getFile(video.getGridfsFileId());
            
            VideoStreamInfo info = VideoStreamInfo.builder()
                    .videoId(videoId)
                    .title(video.getTitle())
                    .contentType(video.getContentType())
                    .fileSize(resource.contentLength())
                    .supportsRangeRequests(true)
                    .chunkSize(1024 * 1024) // 1MB chunks
                    .build();
            
            return ResponseEntity.ok(info);
            
        } catch (Exception e) {
            log.error("Error getting video info: {}", e.getMessage());
            throw new ResourceNotFoundException("Failed to get video info: " + e.getMessage());
        }
    }
    
    @GetMapping("/{videoId}/chunk")
    @Operation(summary = "Obtener chunk de video", description = "Obtiene un chunk específico del video para streaming progresivo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Chunk obtenido exitosamente"),
            @ApiResponse(responseCode = "206", description = "Partial content"),
            @ApiResponse(responseCode = "404", description = "Video no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "416", description = "Rango solicitado no satisfactorio")
    })
    public ResponseEntity<byte[]> getVideoChunk(
            @Parameter(description = "ID del video") @PathVariable String videoId,
            @Parameter(description = "Byte inicial del chunk") @RequestParam long start,
            @Parameter(description = "Byte final del chunk") @RequestParam long end,
            HttpServletResponse response) {
        
        log.info("Video chunk request for video: {} - Range: {}-{}", videoId, start, end);
        
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));
        
        // Verificar permisos
        if (!video.isPublic() && !SecurityUtils.canAccessResource(video.getUploadedBy())) {
            throw new ValidationException("Access denied to this video");
        }
        
        // Verificar que el video esté listo
        if (video.getStatus() != Video.VideoStatus.READY) {
            throw new ValidationException("Video is not ready for streaming");
        }
        
        try {
            GridFsResource resource = gridFsService.getFile(video.getGridfsFileId());
            long fileSize = resource.contentLength();
            
            // Validar rango
            if (start < 0 || end >= fileSize || start > end) {
                response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
                response.setHeader("Content-Range", "bytes */" + fileSize);
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
            }
            
            // Leer chunk específico
            byte[] chunk = readChunk(resource, start, end);
            
            // Configurar headers
            response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
            response.setHeader("Content-Range", String.format("bytes %d-%d/%d", start, end, fileSize));
            response.setHeader("Accept-Ranges", "bytes");
            response.setContentType(video.getContentType());
            response.setContentLength(chunk.length);
            
            return ResponseEntity.ok(chunk);
            
        } catch (Exception e) {
            log.error("Error getting video chunk: {}", e.getMessage());
            throw new ResourceNotFoundException("Failed to get video chunk: " + e.getMessage());
        }
    }
    
    @GetMapping("/{videoId}/progressive-stream")
    @Operation(summary = "Stream progresivo", description = "Stream progresivo del video con soporte para Range Requests optimizado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stream exitoso"),
            @ApiResponse(responseCode = "206", description = "Partial content"),
            @ApiResponse(responseCode = "404", description = "Video no encontrado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public void progressiveStream(
            @Parameter(description = "ID del video") @PathVariable String videoId,
            @Parameter(description = "Token de autorización") @RequestParam(required = false) String token,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        
        log.info("Progressive stream request for video: {} with token: {}", videoId, token != null ? "provided" : "not provided");
        
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));
        
        // Verificar permisos - si hay token, validarlo; si no, verificar si es público
        if (!video.isPublic()) {
            if (token == null || token.trim().isEmpty()) {
                throw new ValidationException("Token required for private video");
            }
            // Aquí podrías validar el token JWT si es necesario
            // Por ahora, solo verificamos que esté presente
            log.info("Token provided for private video: {}", token.substring(0, Math.min(20, token.length())) + "...");
        }
        
        // Verificar que el video esté listo
        if (video.getStatus() != Video.VideoStatus.READY) {
            throw new ValidationException("Video is not ready for streaming");
        }
        
        try {
            GridFsResource resource = gridFsService.getFile(video.getGridfsFileId());
            
            // Configurar headers para streaming progresivo
            response.setHeader("Accept-Ranges", "bytes");
            response.setContentType(video.getContentType());
            response.setHeader("Cache-Control", "public, max-age=3600");
            response.setHeader("X-Content-Type-Options", "nosniff");
            
            // Procesar Range Request
            String rangeHeader = request.getHeader("Range");
            if (rangeHeader != null) {
                streamWithRange(resource, response, rangeHeader, video);
            } else {
                streamFullVideo(resource, response, video);
            }
            
            // Incrementar contador de vistas
            videoService.incrementViewCount(videoId);
            
        } catch (ResourceNotFoundException e) {
            log.error("File not found for video {}: {}", videoId, e.getMessage());
            response.setStatus(HttpStatus.NOT_FOUND.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Video file not found\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Leer un chunk específico del archivo
     */
    private byte[] readChunk(GridFsResource resource, long start, long end) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            // Saltar al inicio del chunk
            long bytesToSkip = start;
            while (bytesToSkip > 0) {
                long skipped = inputStream.skip(bytesToSkip);
                if (skipped == 0) {
                    break;
                }
                bytesToSkip -= skipped;
            }
            
            // Leer el chunk
            int chunkSize = (int) (end - start + 1);
            byte[] chunk = new byte[chunkSize];
            int bytesRead = 0;
            int totalBytesRead = 0;
            
            while (totalBytesRead < chunkSize && (bytesRead = inputStream.read(chunk, totalBytesRead, chunkSize - totalBytesRead)) != -1) {
                totalBytesRead += bytesRead;
            }
            
            // Si no leímos todo el chunk, ajustar el tamaño
            if (totalBytesRead < chunkSize) {
                byte[] adjustedChunk = new byte[totalBytesRead];
                System.arraycopy(chunk, 0, adjustedChunk, 0, totalBytesRead);
                return adjustedChunk;
            }
            
            return chunk;
        }
    }
    
    /**
     * DTO para información del video
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class VideoStreamInfo {
        private String videoId;
        private String title;
        private String contentType;
        private long fileSize;
        private boolean supportsRangeRequests;
        private int chunkSize;
    }
    
}
