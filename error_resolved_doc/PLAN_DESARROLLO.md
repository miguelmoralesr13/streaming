# Plan de Desarrollo - Microservicio de Streaming de Video

## 📋 Resumen del Proyecto
Desarrollo de un microservicio de streaming de video con las siguientes características:
- API REST para subir y obtener videos
- Almacenamiento en MongoDB
- Encriptación/desencriptación de videos
- Streaming con soporte para rangos HTTP
- Autenticación y autorización
- Mejores prácticas de desarrollo

## 🏗️ Arquitectura del Sistema

### Stack Tecnológico
- **Backend**: Spring Boot 3.5.5 + Java 21
- **Base de Datos**: MongoDB
- **Autenticación**: JWT (JSON Web Tokens)
- **Encriptación**: AES-256-GCM
- **Streaming**: HTTP Range Requests
- **Validación**: Bean Validation
- **Documentación**: OpenAPI 3 (Swagger)

### Estructura de Paquetes
```
com.mike.streming/
├── config/          # Configuraciones
├── controller/      # Controladores REST
├── service/         # Lógica de negocio
├── repository/      # Repositorios MongoDB
├── model/          # Entidades y DTOs
├── security/       # Configuración de seguridad
├── encryption/     # Servicios de encriptación
├── exception/      # Manejo de excepciones
└── util/          # Utilidades
```

## 📊 Modelo de Datos

### Entidades Principales

#### User
```json
{
  "id": "ObjectId",
  "username": "String",
  "email": "String",
  "password": "String (hashed)",
  "roles": ["String"],
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime"
}
```

#### Video
```json
{
  "id": "ObjectId",
  "title": "String",
  "description": "String",
  "filename": "String",
  "originalFilename": "String",
  "contentType": "String",
  "size": "Long",
  "duration": "Long",
  "uploadedBy": "ObjectId (User)",
  "isEncrypted": "Boolean",
  "encryptionKey": "String (encrypted)",
  "createdAt": "LocalDateTime",
  "updatedAt": "LocalDateTime"
}
```

#### VideoMetadata
```json
{
  "videoId": "ObjectId",
  "resolution": "String",
  "bitrate": "Long",
  "codec": "String",
  "thumbnail": "String (base64)",
  "tags": ["String"]
}
```

## 🔐 Seguridad y Autenticación

### Flujo de Autenticación
1. **Registro/Login**: Usuario se registra o inicia sesión
2. **JWT Generation**: Se genera token JWT con roles
3. **Request Authorization**: Cada request valida el token
4. **Role-based Access**: Control de acceso basado en roles

### Roles del Sistema
- **USER**: Puede subir, ver y gestionar sus propios videos
- **ADMIN**: Acceso completo al sistema
- **VIEWER**: Solo puede ver videos públicos

## 🔒 Encriptación de Videos

### Estrategia de Encriptación
- **Algoritmo**: AES-256-GCM
- **Key Management**: Cada video tiene su propia clave
- **Key Storage**: Claves encriptadas en base de datos
- **Performance**: Encriptación/desencriptación en streaming

### Flujo de Encriptación
1. **Upload**: Video se encripta antes de almacenar
2. **Storage**: Video encriptado se guarda en MongoDB GridFS
3. **Streaming**: Video se desencripta en tiempo real
4. **Key Rotation**: Soporte para rotación de claves

## 📡 API REST Endpoints

### Autenticación
```
POST /api/auth/register     # Registro de usuario
POST /api/auth/login        # Inicio de sesión
POST /api/auth/refresh      # Renovar token
POST /api/auth/logout       # Cerrar sesión
```

### Gestión de Videos
```
POST /api/videos/upload     # Subir video
GET  /api/videos           # Listar videos del usuario
GET  /api/videos/{id}      # Obtener metadata del video
DELETE /api/videos/{id}    # Eliminar video
PUT  /api/videos/{id}      # Actualizar metadata
```

### Streaming
```
GET /api/videos/{id}/stream # Stream del video (con Range support)
GET /api/videos/{id}/thumbnail # Obtener thumbnail
```

### Administración
```
GET /api/admin/videos      # Listar todos los videos
GET /api/admin/users       # Listar usuarios
PUT /api/admin/videos/{id} # Modificar video (admin)
```

## 🚀 Fases de Desarrollo

### Fase 1: Configuración Base (Día 1-2)
- [x] Configurar dependencias en build.gradle
- [x] Configurar application.properties
- [x] Crear estructura de paquetes
- [x] Configurar MongoDB connection

### Fase 2: Modelos y Repositorios (Día 3-4)
- [ ] Crear entidades User, Video, VideoMetadata
- [ ] Implementar repositorios MongoDB
- [ ] Crear DTOs para requests/responses
- [ ] Configurar validaciones

### Fase 3: Seguridad (Día 5-6)
- [ ] Implementar JWT authentication
- [ ] Configurar Spring Security
- [ ] Crear servicios de autenticación
- [ ] Implementar role-based authorization

### Fase 4: Encriptación (Día 7-8)
- [ ] Implementar servicio de encriptación AES-256-GCM
- [ ] Crear key management system
- [ ] Integrar encriptación en upload/download
- [ ] Testing de encriptación

### Fase 5: Upload y Storage (Día 9-10)
- [ ] Implementar upload de videos
- [ ] Configurar MongoDB GridFS
- [ ] Crear servicio de metadata
- [ ] Implementar validaciones de archivo

### Fase 6: Streaming (Día 11-12)
- [ ] Implementar HTTP Range Requests
- [ ] Crear streaming service
- [ ] Optimizar para diferentes resoluciones
- [ ] Implementar buffering

### Fase 7: API Controllers (Día 13-14)
- [ ] Crear controladores REST
- [ ] Implementar manejo de errores
- [ ] Agregar logging
- [ ] Crear response DTOs

### Fase 8: Testing y Optimización (Día 15-16)
- [ ] Unit tests
- [ ] Integration tests
- [ ] Performance testing
- [ ] Optimización de queries

### Fase 9: Documentación y Deployment (Día 17-18)
- [ ] Documentación OpenAPI/Swagger
- [ ] Docker configuration
- [ ] Environment configurations
- [ ] Deployment scripts

## 🛠️ Dependencias Principales

### Core Spring Boot
- `spring-boot-starter-web`
- `spring-boot-starter-data-mongodb`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`

### Base de Datos
- `spring-data-mongodb`
- `mongodb-driver-sync`

### Seguridad
- `spring-security-jwt`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`

### Encriptación
- `bouncycastle` (para AES-256-GCM)

### Utilidades
- `lombok`
- `mapstruct` (para mapeo de DTOs)
- `springdoc-openapi-starter-webmvc-ui`

### Testing
- `spring-boot-starter-test`
- `testcontainers-mongodb`

## 📈 Consideraciones de Performance

### Optimizaciones de Streaming
- **Chunked Transfer Encoding**: Para videos grandes
- **HTTP Range Requests**: Para seek y resume
- **Compression**: Gzip para metadata
- **Caching**: Headers de cache apropiados

### Optimizaciones de Base de Datos
- **Indexes**: En campos de búsqueda frecuente
- **GridFS**: Para almacenamiento eficiente de archivos
- **Connection Pooling**: Configuración optimizada

### Optimizaciones de Encriptación
- **Streaming Encryption**: Encriptación en chunks
- **Key Caching**: Cache de claves frecuentemente usadas
- **Hardware Acceleration**: Uso de AES-NI cuando disponible

## 🔍 Monitoreo y Logging

### Logging
- **Structured Logging**: JSON format
- **Log Levels**: DEBUG, INFO, WARN, ERROR
- **Sensitive Data**: No logging de claves o passwords

### Métricas
- **Upload/Download Rates**: Métricas de throughput
- **Error Rates**: Tracking de errores
- **Performance**: Latencia de requests

## 🧪 Testing Strategy

### Unit Tests
- **Service Layer**: Lógica de negocio
- **Repository Layer**: Queries MongoDB
- **Security**: Authentication/Authorization

### Integration Tests
- **API Endpoints**: End-to-end testing
- **Database**: MongoDB integration
- **File Upload**: GridFS testing

### Performance Tests
- **Load Testing**: Concurrent uploads/downloads
- **Stress Testing**: Límites del sistema
- **Memory Testing**: Memory leaks detection

## 🚀 Deployment

### Docker
- **Multi-stage Build**: Optimización de imagen
- **Health Checks**: Verificación de salud
- **Environment Variables**: Configuración flexible

### Environment Configurations
- **Development**: Configuración local
- **Staging**: Ambiente de pruebas
- **Production**: Configuración optimizada

## 📚 Documentación

### API Documentation
- **OpenAPI 3**: Especificación completa
- **Swagger UI**: Interface interactiva
- **Postman Collection**: Para testing

### Technical Documentation
- **Architecture**: Diagramas y explicaciones
- **Setup Guide**: Instrucciones de instalación
- **API Reference**: Documentación detallada

---

## ✅ Criterios de Aceptación

### Funcionalidad
- [ ] Usuario puede registrarse y autenticarse
- [ ] Usuario puede subir videos (máx 500MB)
- [ ] Videos se almacenan encriptados en MongoDB
- [ ] Usuario puede ver lista de sus videos
- [ ] Streaming funciona con HTTP Range Requests
- [ ] Usuario puede eliminar sus videos
- [ ] Admin puede gestionar todos los videos

### Seguridad
- [ ] Autenticación JWT implementada
- [ ] Autorización basada en roles
- [ ] Videos encriptados con AES-256-GCM
- [ ] Claves de encriptación protegidas
- [ ] Validación de entrada en todos los endpoints

### Performance
- [ ] Upload de 100MB en menos de 30 segundos
- [ ] Streaming sin buffering para videos < 50MB
- [ ] Soporte para 100 usuarios concurrentes
- [ ] Response time < 200ms para metadata

### Calidad
- [ ] Cobertura de tests > 80%
- [ ] Documentación API completa
- [ ] Logging estructurado implementado
- [ ] Manejo de errores robusto
