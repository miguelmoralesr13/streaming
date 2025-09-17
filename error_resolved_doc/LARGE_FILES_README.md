# 📁 Manejo de Archivos Grandes - Video Streaming

## 🎯 Configuración Actualizada

### ✅ Límites de Subida
- **Tamaño máximo por archivo**: 2GB
- **Tamaño máximo por petición**: 2GB
- **Umbral de escritura a disco**: 2KB
- **Timeout de conexión**: 60 segundos

### 🔧 Configuraciones Aplicadas

#### 1. **Spring Boot Multipart**
```properties
spring.servlet.multipart.max-file-size=2GB
spring.servlet.multipart.max-request-size=2GB
spring.servlet.multipart.file-size-threshold=2KB
spring.servlet.multipart.location=${java.io.tmpdir}
```

#### 2. **Tomcat Server**
```properties
server.tomcat.max-http-post-size=2147483648
server.tomcat.max-http-form-post-size=2147483648
server.tomcat.connection-timeout=60000
```

#### 3. **Docker Resources**
```yaml
deploy:
  resources:
    limits:
      memory: 4G
    reservations:
      memory: 2G
```

## 🚀 Cómo Subir Archivos Grandes

### 1. **Usando Swagger UI**
1. Ve a `http://localhost:8080/api/swagger-ui.html`
2. Autentícate con tu token
3. Usa el endpoint `/videos/upload`
4. Selecciona tu archivo de video (hasta 2GB)
5. Completa los campos de metadata
6. Haz clic en "Execute"

### 2. **Usando cURL (Linux/Mac)**
```bash
# Hacer ejecutable el script
chmod +x test-large-upload.sh

# Ejecutar con tu archivo y token
./test-large-upload.sh /path/to/your/video.mp4 "your-jwt-token"
```

### 3. **Usando PowerShell (Windows)**
```powershell
# Ejecutar con tu archivo y token
.\test-large-upload.ps1 -VideoFile "C:\path\to\your\video.mp4" -Token "your-jwt-token"
```

### 4. **Usando el HTML Player**
1. Abre `video-streaming-player.html`
2. Establece tu token JWT
3. Usa Swagger para subir el video
4. Copia el ID del video devuelto
5. Pégalo en el campo "ID del Video"
6. Haz clic en "Cargar Video"

## ⚠️ Consideraciones Importantes

### 💾 **Memoria y Recursos**
- **RAM mínima recomendada**: 4GB
- **Espacio en disco temporal**: Al menos 4GB libre
- **Conexión estable**: Para archivos > 500MB

### 🔄 **Procesamiento**
- Los archivos se procesan en chunks de 2KB
- Se escriben temporalmente a `/tmp` antes de subir a GridFS
- El proceso puede tomar varios minutos para archivos grandes

### 📊 **Monitoreo**
- Revisa los logs del servidor para ver el progreso
- Usa `docker stats` para monitorear el uso de memoria
- Verifica el espacio en disco con `df -h`

## 🐛 Solución de Problemas

### ❌ **Error: "Request entity too large"**
- Verifica que el archivo no exceda 2GB
- Revisa la configuración de Tomcat
- Reinicia el servidor después de cambios

### ❌ **Error: "Connection timeout"**
- Aumenta el timeout en la configuración
- Verifica la estabilidad de la conexión
- Considera usar un cliente con retry automático

### ❌ **Error: "Out of memory"**
- Aumenta la memoria del contenedor Docker
- Verifica que hay suficiente espacio en `/tmp`
- Considera procesar archivos más pequeños

## 📈 **Optimizaciones Futuras**

### 🔄 **Streaming de Subida**
- Implementar subida por chunks
- Progreso en tiempo real
- Retry automático en caso de fallo

### 🗜️ **Compresión**
- Compresión automática de videos
- Múltiples calidades (720p, 1080p, 4K)
- Thumbnails automáticos

### ☁️ **Almacenamiento**
- Integración con AWS S3
- CDN para distribución global
- Backup automático

## 🎬 **Formatos Soportados**

- **MP4** (recomendado)
- **AVI**
- **MOV**
- **MKV**
- **WebM**
- **FLV**

## 📝 **Notas de Desarrollo**

- Los archivos se almacenan en MongoDB GridFS
- Se mantiene la funcionalidad de encriptación
- El streaming funciona con Range Requests
- Compatible con todos los navegadores modernos

---

**¡Ahora puedes subir videos de hasta 2GB sin problemas!** 🎉
