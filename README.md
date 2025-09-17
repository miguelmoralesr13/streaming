# 🎬 Microservicio de Streaming de Video

Un microservicio completo para streaming de video con encriptación, autenticación JWT y almacenamiento en MongoDB.

## 🚀 Características

- ✅ **Autenticación JWT** con refresh tokens
- ✅ **Encriptación AES-256-GCM** para videos
- ✅ **Streaming con HTTP Range Requests** para seek/resume
- ✅ **Almacenamiento GridFS** en MongoDB
- ✅ **API REST completa** con documentación Swagger
- ✅ **Autorización basada en roles** (USER, ADMIN)
- ✅ **Validación de archivos** y manejo de errores
- ✅ **Upload hasta 500MB**

## 🛠️ Tecnologías

- **Backend**: Spring Boot 3.5.5 + Java 21
- **Base de Datos**: MongoDB + GridFS
- **Seguridad**: JWT + Spring Security
- **Encriptación**: AES-256-GCM (BouncyCastle)
- **Documentación**: OpenAPI 3 (Swagger)
- **Testing**: JUnit 5 + TestContainers

## 📋 Prerrequisitos

- Java 21+
- MongoDB 4.4+
- Gradle 7.0+

## 🚀 Instalación

### 1. Clonar el repositorio
```bash
git clone <repository-url>
cd streming
```

### 2. Configurar MongoDB
```bash
# Instalar MongoDB (Ubuntu/Debian)
sudo apt-get install mongodb

# O usar Docker
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### 3. Configurar variables de entorno
```bash
# Crear archivo .env (opcional)
export MONGODB_HOST=localhost
export MONGODB_PORT=27017
export MONGODB_DATABASE=video_streaming
export JWT_SECRET=mySecretKey123456789012345678901234567890
```

### 4. Ejecutar la aplicación
```bash
./gradlew bootRun
```

La aplicación estará disponible en: `http://localhost:8080`

## 📚 API Documentation

Una vez que la aplicación esté ejecutándose, puedes acceder a:

- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api/api-docs`

## 🔐 Autenticación

### Registro de usuario
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "password123"
  }'
```

### Usar token en requests
```bash
curl -X GET http://localhost:8080/api/videos/my-videos \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🎬 Endpoints Principales

### Autenticación
- `POST /api/auth/register` - Registro
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Renovar token
- `POST /api/auth/logout` - Logout

### Videos
- `POST /api/videos/upload` - Subir video
- `GET /api/videos/{id}` - Obtener video
- `GET /api/videos/my-videos` - Mis videos
- `GET /api/videos/public` - Videos públicos
- `GET /api/videos/search?title=...` - Buscar videos
- `PUT /api/videos/{id}` - Actualizar video
- `DELETE /api/videos/{id}` - Eliminar video

### Streaming
- `GET /api/videos/{id}/stream` - Stream con Range support
- `GET /api/videos/{id}/thumbnail` - Thumbnail
- `GET /api/videos/{id}/download` - Descarga completa

## 🔒 Encriptación

Los videos se pueden encriptar usando AES-256-GCM:

```bash
curl -X POST http://localhost:8080/api/videos/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@video.mp4" \
  -F "title=Mi Video" \
  -F "encryptVideo=true"
```

## 🧪 Testing

```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar tests con cobertura
./gradlew test jacocoTestReport

# Ejecutar tests de integración
./gradlew integrationTest
```

## 📊 Monitoreo

- **Health Check**: `http://localhost:8080/api/actuator/health`
- **Métricas**: `http://localhost:8080/api/actuator/metrics`
- **Info**: `http://localhost:8080/api/actuator/info`

## 🐳 Docker

```bash
# Construir imagen
docker build -t video-streaming .

# Ejecutar contenedor
docker run -p 8080:8080 -p 27017:27017 video-streaming
```

## 🔧 Configuración

### application.properties
```properties
# MongoDB
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=video_streaming

# JWT
jwt.secret=mySecretKey123456789012345678901234567890
jwt.expiration=86400000
jwt.refresh-expiration=604800000

# File Upload
spring.servlet.multipart.max-file-size=500MB
spring.servlet.multipart.max-request-size=500MB

# Encryption
encryption.algorithm=AES/GCM/NoPadding
encryption.key-size=256
encryption.master-key=MyMasterKey123456789012345678901234567890
```

## 🚨 Solución de Problemas

### Error: "JAVA_HOME is not set"
```bash
export JAVA_HOME=/path/to/java21
export PATH=$JAVA_HOME/bin:$PATH
```

### Error: "MongoDB connection failed"
- Verificar que MongoDB esté ejecutándose
- Verificar configuración de conexión en `application.properties`

### Error: "JWT token invalid"
- Verificar que el token no haya expirado
- Verificar que el secret JWT sea el mismo

## 📈 Performance

- **Upload**: Hasta 500MB por video
- **Streaming**: Soporte para HTTP Range Requests
- **Concurrencia**: Hasta 100 usuarios simultáneos
- **Latencia**: < 200ms para metadata

## 🤝 Contribución

1. Fork el proyecto
2. Crear feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push al branch (`git push origin feature/AmazingFeature`)
5. Abrir Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para detalles.

## 👨‍💻 Autor

**Mike** - [GitHub](https://github.com/mike)

## 🙏 Agradecimientos

- Spring Boot Team
- MongoDB Team
- JJWT Library
- BouncyCastle Team
