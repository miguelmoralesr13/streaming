package com.mike.streming.controller;

import com.mike.streming.exception.ResourceNotFoundException;
import com.mike.streming.model.Video;
import com.mike.streming.repository.VideoRepository;
import com.mike.streming.service.GridFsService;
import com.mike.streming.service.VideoService;
import com.mike.streming.util.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test para verificar el manejo de errores en StreamingController
 */
@WebMvcTest(StreamingController.class)
@ActiveProfiles("test")
@DisplayName("StreamingController Error Handling Tests")
class StreamingControllerErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VideoRepository videoRepository;

    @MockBean
    private GridFsService gridFsService;

    @MockBean
    private VideoService videoService;

    @MockBean
    private SecurityUtils securityUtils;

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Debería devolver 404 cuando el video no existe")
    void shouldReturn404WhenVideoNotFound() throws Exception {
        // Given
        String videoId = "nonexistent-video-id";
        when(videoRepository.findById(videoId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/streaming/{videoId}", videoId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Video not found with id: " + videoId));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Debería devolver 404 cuando el archivo de GridFS no existe")
    void shouldReturn404WhenGridFsFileNotFound() throws Exception {
        // Given
        String videoId = "video-with-missing-file";
        Video video = Video.builder()
                .id(videoId)
                .title("Test Video")
                .isPublic(true)
                .status(Video.VideoStatus.READY)
                .gridfsFileId("68c9e86128df2a17942b991e")
                .uploadedBy("testuser")
                .build();

        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(securityUtils.canAccessResource(anyString())).thenReturn(true);
        when(gridFsService.getFile(anyString())).thenThrow(new ResourceNotFoundException("File not found with id: 68c9e86128df2a17942b991e"));

        // When & Then
        mockMvc.perform(get("/api/streaming/{videoId}", videoId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Video file not found"))
                .andExpect(jsonPath("$.message").value("File not found with id: 68c9e86128df2a17942b991e"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Debería devolver 403 cuando el usuario no tiene acceso al video")
    void shouldReturn403WhenAccessDenied() throws Exception {
        // Given
        String videoId = "private-video";
        Video video = Video.builder()
                .id(videoId)
                .title("Private Video")
                .isPublic(false)
                .status(Video.VideoStatus.READY)
                .gridfsFileId("some-file-id")
                .uploadedBy("otheruser")
                .build();

        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(securityUtils.canAccessResource("otheruser")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/streaming/{videoId}", videoId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Access denied to this video"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Debería devolver 400 cuando el video no está listo para streaming")
    void shouldReturn400WhenVideoNotReady() throws Exception {
        // Given
        String videoId = "processing-video";
        Video video = Video.builder()
                .id(videoId)
                .title("Processing Video")
                .isPublic(true)
                .status(Video.VideoStatus.PROCESSING)
                .gridfsFileId("some-file-id")
                .uploadedBy("testuser")
                .build();

        when(videoRepository.findById(videoId)).thenReturn(Optional.of(video));
        when(securityUtils.canAccessResource(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/streaming/{videoId}", videoId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Video is not ready for streaming"));
    }
}
