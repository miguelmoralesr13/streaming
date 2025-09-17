package com.mike.streming.service;

import com.mike.streming.dto.VideoResponse;
import com.mike.streming.dto.VideoUploadRequest;
import com.mike.streming.encryption.VideoEncryptionService;
import com.mike.streming.exception.ResourceNotFoundException;
import com.mike.streming.exception.ValidationException;
import com.mike.streming.model.Video;
import com.mike.streming.repository.VideoRepository;
import com.mike.streming.util.FileUtils;
import com.mike.streming.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de videos
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {
    
    private final VideoRepository videoRepository;
    private final VideoEncryptionService videoEncryptionService;
    private final GridFsService gridFsService;
    
    /**
     * Subir video
     */
    @Transactional
    public VideoResponse uploadVideo(MultipartFile file, VideoUploadRequest request) {
        log.info("Uploading video: {}", file.getOriginalFilename());
        
        // Validar archivo
        FileUtils.validateVideoFile(file);
        
        // Obtener usuario actual
        String currentUserId = SecurityUtils.getCurrentUserId();
        
        // Generar clave de encriptación si es necesario
        String encryptionKey = null;
        if (request.isEncryptVideo()) {
            encryptionKey = videoEncryptionService.generateVideoEncryptionKey();
        }
        
        // Crear entidad Video
        Video video = Video.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .uploadedBy(currentUserId)
                .isEncrypted(request.isEncryptVideo())
                .encryptionKey(encryptionKey != null ? videoEncryptionService.encryptVideoKey(encryptionKey) : null)
                .tags(request.getTags())
                .isPublic(request.isPublic())
                .viewCount(0L)
                .status(Video.VideoStatus.UPLOADING)
                .processingStatus(Video.ProcessingStatus.PENDING)
                .uploadProgress(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        video = videoRepository.save(video);
        
        try {
            // Guardar archivo en GridFS
            String gridfsFileId = gridFsService.storeFile(file, video.getId());
            video.setGridfsFileId(gridfsFileId);
            
            // Encriptar si es necesario
            if (request.isEncryptVideo() && encryptionKey != null) {
                video.setProcessingStatus(Video.ProcessingStatus.ENCRYPTING);
                videoRepository.save(video);
                
                // TODO: Implementar encriptación asíncrona
                // videoEncryptionService.encryptVideo(file.getInputStream(), encryptionKey);
            }
            
            // Generar thumbnail
            video.setProcessingStatus(Video.ProcessingStatus.GENERATING_THUMBNAIL);
            videoRepository.save(video);
            
            // TODO: Implementar generación de thumbnail
            // String thumbnailId = thumbnailService.generateThumbnail(file);
            // video.setThumbnailId(thumbnailId);
            
            // Completar procesamiento
            video.setStatus(Video.VideoStatus.READY);
            video.setProcessingStatus(Video.ProcessingStatus.COMPLETED);
            video.setUploadProgress(100);
            video.setUpdatedAt(LocalDateTime.now());
            
            video = videoRepository.save(video);
            
            log.info("Video uploaded successfully: {}", video.getId());
            return mapToVideoResponse(video);
            
        } catch (Exception e) {
            log.error("Error uploading video: {}", e.getMessage());
            video.setStatus(Video.VideoStatus.ERROR);
            video.setProcessingStatus(Video.ProcessingStatus.FAILED);
            videoRepository.save(video);
            throw new ValidationException("Failed to upload video: " + e.getMessage());
        }
    }
    
    /**
     * Obtener video por ID
     */
    public VideoResponse getVideoById(String videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));
        
        // Verificar permisos
        if (!video.isPublic() && !SecurityUtils.canAccessResource(video.getUploadedBy())) {
            throw new ValidationException("Access denied to this video");
        }
        
        return mapToVideoResponse(video);
    }
    
    /**
     * Obtener videos del usuario actual
     */
    public Page<VideoResponse> getUserVideos(Pageable pageable) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Page<Video> videos = videoRepository.findByUploadedBy(currentUserId, pageable);
        return videos.map(this::mapToVideoResponse);
    }
    
    /**
     * Obtener videos públicos
     */
    public Page<VideoResponse> getPublicVideos(Pageable pageable) {
        Page<Video> videos = videoRepository.findByIsPublicTrue(pageable);
        return videos.map(this::mapToVideoResponse);
    }
    
    /**
     * Buscar videos por título
     */
    public Page<VideoResponse> searchVideosByTitle(String title, Pageable pageable) {
        Page<Video> videos = videoRepository.findPublicVideosByTitleContaining(title, pageable);
        return videos.map(this::mapToVideoResponse);
    }
    
    /**
     * Eliminar video
     */
    @Transactional
    public void deleteVideo(String videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));
        
        // Verificar permisos
        if (!SecurityUtils.canAccessResource(video.getUploadedBy())) {
            throw new ValidationException("Access denied to delete this video");
        }
        
        try {
            // Eliminar archivo de GridFS
            if (video.getGridfsFileId() != null) {
                gridFsService.deleteFile(video.getGridfsFileId());
            }
            
            // Eliminar thumbnail
            if (video.getThumbnailId() != null) {
                gridFsService.deleteFile(video.getThumbnailId());
            }
            
            // Marcar como eliminado
            video.setStatus(Video.VideoStatus.DELETED);
            videoRepository.save(video);
            
            log.info("Video deleted successfully: {}", videoId);
            
        } catch (Exception e) {
            log.error("Error deleting video: {}", e.getMessage());
            throw new ValidationException("Failed to delete video: " + e.getMessage());
        }
    }
    
    /**
     * Actualizar metadata del video
     */
    @Transactional
    public VideoResponse updateVideo(String videoId, VideoUploadRequest request) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));
        
        // Verificar permisos
        if (!SecurityUtils.canAccessResource(video.getUploadedBy())) {
            throw new ValidationException("Access denied to update this video");
        }
        
        video.setTitle(request.getTitle());
        video.setDescription(request.getDescription());
        video.setTags(request.getTags());
        video.setPublic(request.isPublic());
        video.setUpdatedAt(LocalDateTime.now());
        
        video = videoRepository.save(video);
        
        return mapToVideoResponse(video);
    }
    
    /**
     * Incrementar contador de vistas
     */
    @Transactional
    public void incrementViewCount(String videoId) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + videoId));
        
        video.setViewCount(video.getViewCount() + 1);
        videoRepository.save(video);
    }
    
    
    /**
     * Mapear Video a VideoResponse
     */
    private VideoResponse mapToVideoResponse(Video video) {
        return VideoResponse.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .originalFilename(video.getOriginalFilename())
                .contentType(video.getContentType())
                .size(video.getSize())
                .duration(video.getDuration())
                .uploadedBy(video.getUploadedBy())
                .isEncrypted(video.isEncrypted())
                .resolution(video.getResolution())
                .bitrate(video.getBitrate())
                .codec(video.getCodec())
                .tags(video.getTags())
                .isPublic(video.isPublic())
                .viewCount(video.getViewCount())
                .status(video.getStatus())
                .processingStatus(video.getProcessingStatus())
                .createdAt(video.getCreatedAt())
                .updatedAt(video.getUpdatedAt())
                .uploadProgress(video.getUploadProgress())
                .thumbnailUrl(video.getThumbnailId() != null ? "/api/videos/" + video.getId() + "/thumbnail" : null)
                .streamUrl("/api/videos/" + video.getId() + "/stream")
                .build();
    }
}
