package com.mike.streming.dto;

import com.mike.streming.model.Video;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuesta de videos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoResponse {
    
    private String id;
    private String title;
    private String description;
    private String originalFilename;
    private String contentType;
    private Long size;
    private Long duration;
    private String uploadedBy;
    private boolean isEncrypted;
    private String resolution;
    private Long bitrate;
    private String codec;
    private List<String> tags;
    private boolean isPublic;
    private Long viewCount;
    private Video.VideoStatus status;
    private Video.ProcessingStatus processingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer uploadProgress;
    private String thumbnailUrl;
    private String streamUrl;
}
