package com.mike.streming.repository;

import com.mike.streming.model.VideoMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad VideoMetadata
 */
@Repository
public interface VideoMetadataRepository extends MongoRepository<VideoMetadata, String> {
    
    /**
     * Buscar metadata por video ID
     */
    Optional<VideoMetadata> findByVideoId(String videoId);
    
    /**
     * Buscar videos por resolución
     */
    @Query("{'width': ?0, 'height': ?1}")
    List<VideoMetadata> findByResolution(Integer width, Integer height);
    
    /**
     * Buscar videos por codec
     */
    List<VideoMetadata> findByCodec(String codec);
    
    /**
     * Buscar videos por rango de duración
     */
    @Query("{'duration': {$gte: ?0, $lte: ?1}}")
    List<VideoMetadata> findByDurationBetween(Double minDuration, Double maxDuration);
    
    /**
     * Buscar videos por rango de bitrate
     */
    @Query("{'bitrate': {$gte: ?0, $lte: ?1}}")
    List<VideoMetadata> findByBitrateBetween(Long minBitrate, Long maxBitrate);
    
    /**
     * Buscar videos con audio
     */
    List<VideoMetadata> findByHasAudioTrue();
    
    /**
     * Buscar videos sin audio
     */
    List<VideoMetadata> findByHasAudioFalse();
    
    /**
     * Buscar videos por formato de contenedor
     */
    List<VideoMetadata> findByContainerFormat(String containerFormat);
    
    /**
     * Buscar videos por codec de audio
     */
    List<VideoMetadata> findByAudioCodec(String audioCodec);
    
    /**
     * Buscar videos por número de canales de audio
     */
    List<VideoMetadata> findByAudioChannels(Integer audioChannels);
    
    /**
     * Eliminar metadata por video ID
     */
    void deleteByVideoId(String videoId);
}
