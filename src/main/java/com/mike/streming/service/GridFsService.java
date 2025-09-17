package com.mike.streming.service;

import com.mike.streming.exception.FileUploadException;
import com.mike.streming.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Servicio para manejo de archivos con GridFS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GridFsService {
    
    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;
    
    /**
     * Almacenar archivo en GridFS
     */
    public String storeFile(MultipartFile file, String videoId) {
        try {
            log.info("Storing file in GridFS: {}", file.getOriginalFilename());
            
            String filename = generateFilename(file.getOriginalFilename(), videoId);
            
            String fileId = gridFsTemplate.store(
                    file.getInputStream(),
                    filename,
                    file.getContentType(),
                    createMetadata(file, videoId)
            ).toString();
            
            log.info("File stored successfully with ID: {}", fileId);
            return fileId;
            
        } catch (IOException e) {
            log.error("Error storing file in GridFS: {}", e.getMessage());
            throw new FileUploadException("Failed to store file: " + e.getMessage());
        }
    }
    
    /**
     * Almacenar stream de datos en GridFS
     */
    public String storeStream(InputStream inputStream, String filename, String contentType, Object metadata) {
        try {
            log.info("Storing stream in GridFS: {}", filename);
            
            String fileId = gridFsTemplate.store(
                    inputStream,
                    filename,
                    contentType,
                    metadata
            ).toString();
            
            log.info("Stream stored successfully with ID: {}", fileId);
            return fileId;
            
        } catch (Exception e) {
            log.error("Error storing stream in GridFS: {}", e.getMessage());
            throw new FileUploadException("Failed to store stream: " + e.getMessage());
        }
    }
    
    /**
     * Obtener archivo de GridFS
     */
    public GridFsResource getFile(String fileId) {
        log.debug("Retrieving file from GridFS: {}", fileId);
        
        GridFsResource resource = null;
        
        // Estrategia 1: Buscar por ObjectId (más confiable para IDs)
        try {
            org.bson.types.ObjectId objectId = new org.bson.types.ObjectId(fileId);
            
            // Buscar el archivo usando GridFsOperations
            org.springframework.data.mongodb.core.query.Query query = 
                org.springframework.data.mongodb.core.query.Query.query(
                    org.springframework.data.mongodb.core.query.Criteria.where("_id").is(objectId)
                );
            
            com.mongodb.client.gridfs.model.GridFSFile gridFsFile = 
                gridFsOperations.findOne(query);
            
            if (gridFsFile != null) {
                resource = gridFsTemplate.getResource(gridFsFile);
                log.debug("Found file using ObjectId: {}", fileId);
            }
        } catch (IllegalArgumentException e) {
            log.debug("Invalid ObjectId format, trying other methods: {}", fileId);
        }
        
        // Estrategia 2: Si no se encuentra, buscar por ID directo (para nombres de archivo)
        if (resource == null) {
            try {
                resource = gridFsTemplate.getResource(fileId);
                if (resource != null) {
                    log.debug("Found file using direct ID: {}", fileId);
                }
            } catch (Exception e) {
                log.debug("Error searching by direct ID: {}", e.getMessage());
            }
        }
        
        // Estrategia 3: Si aún no se encuentra, buscar por query con String
        if (resource == null) {
            try {
                org.springframework.data.mongodb.core.query.Query query = 
                    org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("_id").is(fileId)
                    );
                
                com.mongodb.client.gridfs.model.GridFSFile gridFsFile = 
                    gridFsOperations.findOne(query);
                
                if (gridFsFile != null) {
                    resource = gridFsTemplate.getResource(gridFsFile);
                    log.debug("Found file using query: {}", fileId);
                }
            } catch (Exception e) {
                log.debug("Error searching by query: {}", e.getMessage());
            }
        }
        
        // Si aún no se encuentra, lanzar excepción
        if (resource == null) {
            log.error("File not found with any method for id: {}", fileId);
            throw new ResourceNotFoundException("File not found with id: " + fileId);
        }
        
        // Verificar si el archivo realmente existe
        try {
            if (!resource.exists()) {
                log.error("File resource exists but file is not accessible: {}", fileId);
                throw new ResourceNotFoundException("File not found with id: " + fileId);
            }
        } catch (Exception e) {
            log.error("Error verifying file existence: {}", e.getMessage());
            throw new ResourceNotFoundException("File not found with id: " + fileId);
        }
        
        log.debug("Successfully retrieved file: {}", fileId);
        return resource;
    }
    
    /**
     * Obtener stream de archivo
     */
    public InputStream getFileStream(String fileId) {
        GridFsResource resource = getFile(fileId);
        try {
            return resource.getInputStream();
        } catch (IOException e) {
            log.error("Error getting file stream: {}", e.getMessage());
            throw new FileUploadException("Failed to get file stream: " + e.getMessage());
        }
    }
    
    /**
     * Eliminar archivo de GridFS
     */
    public void deleteFile(String fileId) {
        try {
            log.info("Deleting file from GridFS: {}", fileId);
            
            gridFsTemplate.delete(org.springframework.data.mongodb.core.query.Query.query(
                    org.springframework.data.mongodb.core.query.Criteria.where("_id").is(fileId)
            ));
            
            log.info("File deleted successfully: {}", fileId);
            
        } catch (Exception e) {
            log.error("Error deleting file from GridFS: {}", e.getMessage());
            throw new FileUploadException("Failed to delete file: " + e.getMessage());
        }
    }
    
    /**
     * Verificar si archivo existe
     */
    public boolean fileExists(String fileId) {
        try {
            GridFsResource resource = gridFsTemplate.getResource(fileId);
            return resource != null && resource.exists();
        } catch (Exception e) {
            log.debug("File does not exist: {}", fileId);
            return false;
        }
    }
    
    /**
     * Diagnosticar problemas con archivos en GridFS
     */
    public void diagnoseFile(String fileId) {
        log.info("=== DIAGNÓSTICO DE ARCHIVO GRIDFS ===");
        log.info("File ID: {}", fileId);
        
        // Verificar formato del ID
        try {
            org.bson.types.ObjectId objectId = new org.bson.types.ObjectId(fileId);
            log.info("✅ ID es un ObjectId válido: {}", objectId);
        } catch (IllegalArgumentException e) {
            log.warn("❌ ID no es un ObjectId válido: {}", e.getMessage());
        }
        
        // Buscar por ID directo
        try {
            GridFsResource resource = gridFsTemplate.getResource(fileId);
            if (resource != null) {
                log.info("✅ Archivo encontrado por ID directo");
                log.info("   - Existe: {}", resource.exists());
                log.info("   - Nombre: {}", resource.getFilename());
                log.info("   - Tamaño: {}", resource.contentLength());
            } else {
                log.warn("❌ Archivo NO encontrado por ID directo");
            }
        } catch (Exception e) {
            log.error("❌ Error buscando por ID directo: {}", e.getMessage());
        }
        
        // Buscar por ObjectId
        try {
            org.bson.types.ObjectId objectId = new org.bson.types.ObjectId(fileId);
            
            org.springframework.data.mongodb.core.query.Query query = 
                org.springframework.data.mongodb.core.query.Query.query(
                    org.springframework.data.mongodb.core.query.Criteria.where("_id").is(objectId)
                );
            
            com.mongodb.client.gridfs.model.GridFSFile gridFsFile = 
                gridFsOperations.findOne(query);
            
            if (gridFsFile != null) {
                GridFsResource resource = gridFsTemplate.getResource(gridFsFile);
                log.info("✅ Archivo encontrado por ObjectId");
                log.info("   - Existe: {}", resource.exists());
                log.info("   - Nombre: {}", resource.getFilename());
                log.info("   - Tamaño: {}", resource.contentLength());
            } else {
                log.warn("❌ Archivo NO encontrado por ObjectId");
            }
        } catch (Exception e) {
            log.error("❌ Error buscando por ObjectId: {}", e.getMessage());
        }
        
        // Buscar por query
        try {
            org.springframework.data.mongodb.core.query.Query query = 
                org.springframework.data.mongodb.core.query.Query.query(
                    org.springframework.data.mongodb.core.query.Criteria.where("_id").is(fileId)
                );
            
            com.mongodb.client.gridfs.model.GridFSFile gridFsFile = 
                gridFsOperations.findOne(query);
            
            if (gridFsFile != null) {
                GridFsResource resource = gridFsTemplate.getResource(gridFsFile);
                log.info("✅ Archivo encontrado por query");
                log.info("   - Existe: {}", resource.exists());
                log.info("   - Nombre: {}", resource.getFilename());
                log.info("   - Tamaño: {}", resource.contentLength());
            } else {
                log.warn("❌ Archivo NO encontrado por query");
            }
        } catch (Exception e) {
            log.error("❌ Error buscando por query: {}", e.getMessage());
        }
        
        // Listar todos los archivos en GridFS (para debugging)
        try {
            org.springframework.data.mongodb.core.query.Query allQuery = 
                org.springframework.data.mongodb.core.query.Query.query(
                    org.springframework.data.mongodb.core.query.Criteria.where("_id").exists(true)
                );
            
            java.util.List<com.mongodb.client.gridfs.model.GridFSFile> allFiles = 
                gridFsOperations.find(allQuery).into(new java.util.ArrayList<>());
            
            log.info("📋 Total de archivos en GridFS: {}", allFiles.size());
            if (!allFiles.isEmpty()) {
                log.info("📋 Primeros 5 archivos:");
                for (int i = 0; i < Math.min(5, allFiles.size()); i++) {
                    com.mongodb.client.gridfs.model.GridFSFile file = allFiles.get(i);
                    GridFsResource resource = gridFsTemplate.getResource(file);
                    log.info("   [{}] ID: {}, Nombre: {}, Tamaño: {}", 
                        i, file.getObjectId(), file.getFilename(), file.getLength());
                }
            }
        } catch (Exception e) {
            log.error("❌ Error listando archivos: {}", e.getMessage());
        }
        
        log.info("=== FIN DIAGNÓSTICO ===");
    }
    
    /**
     * Obtener metadata del archivo
     */
    public Object getFileMetadata(String fileId) {
        try {
            // Buscar el archivo en GridFS para obtener metadata
            org.springframework.data.mongodb.core.query.Query query = 
                org.springframework.data.mongodb.core.query.Query.query(
                    org.springframework.data.mongodb.core.query.Criteria.where("_id").is(fileId)
                );
            
            return gridFsTemplate.findOne(query);
        } catch (Exception e) {
            log.error("Error getting file metadata: {}", e.getMessage());
            throw new FileUploadException("Failed to get file metadata: " + e.getMessage());
        }
    }
    
    /**
     * Generar nombre de archivo único
     */
    private String generateFilename(String originalFilename, String videoId) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return videoId + "_" + System.currentTimeMillis() + extension;
    }
    
    /**
     * Crear metadata para el archivo
     */
    private Object createMetadata(MultipartFile file, String videoId) {
        return new java.util.HashMap<String, Object>() {{
            put("videoId", videoId);
            put("originalFilename", file.getOriginalFilename());
            put("contentType", file.getContentType());
            put("size", file.getSize());
            put("uploadDate", java.time.LocalDateTime.now());
        }};
    }
}
