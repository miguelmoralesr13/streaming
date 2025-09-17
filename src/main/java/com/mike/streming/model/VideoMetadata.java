package com.mike.streming.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

/**
 * Entidad VideoMetadata para información técnica detallada de los videos
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "video_metadata")
public class VideoMetadata {
    
    @Id
    private String id;
    
    @Field("video_id")
    private String videoId;
    
    @Field("width")
    private Integer width;
    
    @Field("height")
    private Integer height;
    
    @Field("aspect_ratio")
    private String aspectRatio;
    
    @Field("frame_rate")
    private Double frameRate;
    
    @Field("bitrate")
    private Long bitrate;
    
    @Field("codec")
    private String codec;
    
    @Field("profile")
    private String profile;
    
    @Field("level")
    private String level;
    
    @Field("pixel_format")
    private String pixelFormat;
    
    @Field("color_space")
    private String colorSpace;
    
    @Field("audio_codec")
    private String audioCodec;
    
    @Field("audio_bitrate")
    private Long audioBitrate;
    
    @Field("audio_channels")
    private Integer audioChannels;
    
    @Field("audio_sample_rate")
    private Integer audioSampleRate;
    
    @Field("duration")
    private Double duration;
    
    @Field("file_size")
    private Long fileSize;
    
    @Field("container_format")
    private String containerFormat;
    
    @Field("has_audio")
    private Boolean hasAudio;
    
    @Field("has_video")
    private Boolean hasVideo;
    
    @Field("streams")
    private List<StreamInfo> streams;
    
    @Field("technical_details")
    private Map<String, Object> technicalDetails;
    
    /**
     * Información de streams individuales
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamInfo {
        private Integer index;
        private String codec;
        private String type; // video, audio, subtitle
        private Long bitrate;
        private Map<String, Object> properties;
    }
}
