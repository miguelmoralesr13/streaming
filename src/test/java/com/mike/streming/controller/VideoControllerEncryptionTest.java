package com.mike.streming.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mike.streming.dto.VideoResponse;
import com.mike.streming.model.Video;
import com.mike.streming.service.VideoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para el controlador de videos con encriptación
 */
@WebMvcTest(VideoController.class)
@DisplayName("VideoController Encryption Integration Tests")
class VideoControllerEncryptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoService videoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Debería subir un video con encriptación habilitada")
    void shouldUploadVideoWithEncryptionEnabled() throws Exception {
        // Given
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "Contenido de video de prueba".getBytes()
        );

        VideoResponse mockResponse = VideoResponse.builder()
                .id("video123")
                .title("Video de Prueba")
                .description("Video de prueba con encriptación")
                .isEncrypted(true)
                .size(1024L)
                .status(Video.VideoStatus.PROCESSING)
                .build();

        when(videoService.uploadVideo(any(), any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(multipart("/api/videos/upload")
                        .file(videoFile)
                        .param("title", "Video de Prueba")
                        .param("description", "Video de prueba con encriptación")
                        .param("tags", "test,prueba,encriptacion")
                        .param("isPublic", "false")
                        .param("encryptVideo", "true")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("video123"))
                .andExpect(jsonPath("$.title").value("Video de Prueba"))
                .andExpect(jsonPath("$.description").value("Video de prueba con encriptación"))
                .andExpect(jsonPath("$.isEncrypted").value(true))
                .andExpect(jsonPath("$.size").value(1024))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Debería subir un video sin encriptación")
    void shouldUploadVideoWithoutEncryption() throws Exception {
        // Given
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "Contenido de video de prueba".getBytes()
        );

        VideoResponse mockResponse = VideoResponse.builder()
                .id("video456")
                .title("Video Sin Encriptación")
                .description("Video de prueba sin encriptación")
                .isEncrypted(false)
                .size(1024L)
                .status(Video.VideoStatus.PROCESSING)
                .build();

        when(videoService.uploadVideo(any(), any())).thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(multipart("/api/videos/upload")
                        .file(videoFile)
                        .param("title", "Video Sin Encriptación")
                        .param("description", "Video de prueba sin encriptación")
                        .param("isPublic", "true")
                        .param("encryptVideo", "false")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("video456"))
                .andExpect(jsonPath("$.title").value("Video Sin Encriptación"))
                .andExpect(jsonPath("$.description").value("Video de prueba sin encriptación"))
                .andExpect(jsonPath("$.isEncrypted").value(false))
                .andExpect(jsonPath("$.size").value(1024))
                .andExpect(jsonPath("$.status").value("PROCESSING"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Debería rechazar archivos que no son videos")
    void shouldRejectNonVideoFiles() throws Exception {
        // Given
        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Este es un archivo de texto".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/videos/upload")
                        .file(textFile)
                        .param("title", "Archivo de Texto")
                        .param("encryptVideo", "false")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Debería rechazar archivos vacíos")
    void shouldRejectEmptyFiles() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.mp4",
                "video/mp4",
                new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/videos/upload")
                        .file(emptyFile)
                        .param("title", "Archivo Vacío")
                        .param("encryptVideo", "false")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Debería rechazar requests sin título")
    void shouldRejectRequestsWithoutTitle() throws Exception {
        // Given
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "Contenido de video de prueba".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/videos/upload")
                        .file(videoFile)
                        .param("encryptVideo", "false")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Debería requerir autenticación para subir videos")
    void shouldRequireAuthenticationForVideoUpload() throws Exception {
        // Given
        MockMultipartFile videoFile = new MockMultipartFile(
                "file",
                "test-video.mp4",
                "video/mp4",
                "Contenido de video de prueba".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/videos/upload")
                        .file(videoFile)
                        .param("title", "Video de Prueba")
                        .param("encryptVideo", "false")
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Debería manejar diferentes formatos de video")
    void shouldHandleDifferentVideoFormats() throws Exception {
        // Given
        String[] formats = {"mp4", "avi", "mov", "mkv"};
        String[] mimeTypes = {"video/mp4", "video/x-msvideo", "video/quicktime", "video/x-matroska"};

        for (int i = 0; i < formats.length; i++) {
            MockMultipartFile videoFile = new MockMultipartFile(
                    "file",
                    "test-video." + formats[i],
                    mimeTypes[i],
                    "Contenido de video de prueba".getBytes()
            );

            VideoResponse mockResponse = VideoResponse.builder()
                    .id("video" + i)
                    .title("Video " + formats[i].toUpperCase())
                    .isEncrypted(false)
                    .size(1024L)
                    .status(Video.VideoStatus.PROCESSING)
                    .build();

            when(videoService.uploadVideo(any(), any())).thenReturn(mockResponse);

            // When & Then
            mockMvc.perform(multipart("/api/videos/upload")
                            .file(videoFile)
                            .param("title", "Video " + formats[i].toUpperCase())
                            .param("encryptVideo", "false")
                            .with(csrf())
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Video " + formats[i].toUpperCase()));
        }
    }
}
