package com.mike.streming.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad Video para manejo de videos del sistema
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "videos")
public class Video {
    
    @Id
    private String id;
    
    @Field("title")
    private String title;
    
    @Field("description")
    private String description;
    
    @Field("filename")
    private String filename;
    
    @Field("original_filename")
    private String originalFilename;
    
    @Field("content_type")
    private String contentType;
    
    @Field("size")
    private Long size;
    
    @Field("duration")
    private Long duration; // en segundos
    
    @Indexed
    @Field("uploaded_by")
    private String uploadedBy; // User ID
    
    @Field("is_encrypted")
    private boolean isEncrypted;
    
    @Field("encryption_key")
    private String encryptionKey; // Clave encriptada
    
    @Field("gridfs_file_id")
    private String gridfsFileId; // ID del archivo en GridFS
    
    @Field("thumbnail_id")
    private String thumbnailId; // ID del thumbnail en GridFS
    
    @Field("resolution")
    private String resolution; // ej: "1920x1080"
    
    @Field("bitrate")
    private Long bitrate;
    
    @Field("codec")
    private String codec;
    
    @Field("tags")
    private List<String> tags;
    
    @Field("is_public")
    private boolean isPublic;
    
    @Field("view_count")
    private Long viewCount;
    
    @Field("status")
    private VideoStatus status;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("upload_progress")
    private Integer uploadProgress; // 0-100
    
    @Field("processing_status")
    private ProcessingStatus processingStatus;
    
    /**
     * Estados del video
     */
    public enum VideoStatus {
        UPLOADING,
        PROCESSING,
        READY,
        ERROR,
        DELETED
    }
    
    /**
     * Estados de procesamiento
     */
    public enum ProcessingStatus {
        PENDING,
        ENCRYPTING,
        GENERATING_THUMBNAIL,
        COMPLETED,
        FAILED
    }
}
