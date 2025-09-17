package com.mike.streming.repository;

import com.mike.streming.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Video
 */
@Repository
public interface VideoRepository extends MongoRepository<Video, String> {
    
    /**
     * Buscar videos por usuario
     */
    Page<Video> findByUploadedBy(String uploadedBy, Pageable pageable);
    
    /**
     * Buscar videos públicos
     */
    Page<Video> findByIsPublicTrue(Pageable pageable);
    
    /**
     * Buscar videos por estado
     */
    List<Video> findByStatus(Video.VideoStatus status);
    
    /**
     * Buscar videos por estado de procesamiento
     */
    List<Video> findByProcessingStatus(Video.ProcessingStatus processingStatus);
    
    /**
     * Buscar videos por título (búsqueda parcial)
     */
    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    Page<Video> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * Buscar videos por tags
     */
    @Query("{'tags': {$in: ?0}}")
    Page<Video> findByTagsIn(List<String> tags, Pageable pageable);
    
    /**
     * Buscar videos por usuario y estado
     */
    List<Video> findByUploadedByAndStatus(String uploadedBy, Video.VideoStatus status);
    
    /**
     * Buscar videos públicos por título
     */
    @Query("{'isPublic': true, 'title': {$regex: ?0, $options: 'i'}}")
    Page<Video> findPublicVideosByTitleContaining(String title, Pageable pageable);
    
    /**
     * Buscar videos por rango de duración
     */
    @Query("{'duration': {$gte: ?0, $lte: ?1}}")
    Page<Video> findByDurationBetween(Long minDuration, Long maxDuration, Pageable pageable);
    
    /**
     * Buscar videos por resolución
     */
    List<Video> findByResolution(String resolution);
    
    /**
     * Contar videos por usuario
     */
    long countByUploadedBy(String uploadedBy);
    
    /**
     * Contar videos públicos
     */
    long countByIsPublicTrue();
    
    /**
     * Buscar videos más vistos
     */
    @Query("{'isPublic': true}")
    Page<Video> findTopVideosByViewCount(Pageable pageable);
    
    /**
     * Buscar videos recientes
     */
    @Query("{'isPublic': true}")
    Page<Video> findRecentPublicVideos(Pageable pageable);
}
