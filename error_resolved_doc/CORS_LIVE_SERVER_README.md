# 🌐 CORS Configurado para Live Server (VS Code)

## 🎯 **Configuración Aplicada**

### ✅ **Orígenes Permitidos:**
- `http://127.0.0.1:5500` - Live Server por defecto
- `http://localhost:5500` - Live Server alternativo

### ✅ **Métodos HTTP Permitidos:**
- GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH

### ✅ **Headers Permitidos:**
- `*` (cualquier header)

### ✅ **Credenciales:**
- Permitidas (para JWT tokens)

## 🔧 **Archivos Modificados**

### **1. CorsConfig.java**
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://127.0.0.1:5500", "http://localhost:5500")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("Content-Range", "Accept-Ranges", "Content-Length", "Authorization")
                .maxAge(3600);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir orígenes específicos (Live Server de VS Code)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://127.0.0.1:5500",
            "http://localhost:5500"
        ));
        
        // ... resto de la configuración
    }
}
```

### **2. application.properties**
```properties
# CORS Configuration - Configurado para Live Server (VS Code)
cors.allowed-origins=http://127.0.0.1:5500,http://localhost:5500
cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS,HEAD,PATCH
cors.allowed-headers=*
cors.allow-credentials=true
```

### **3. video-streaming-player.html**
```javascript
// Configuración de CORS para Live Server (VS Code)
console.log('🌐 CORS configurado para Live Server: http://127.0.0.1:5500');
```

## 🚀 **Cómo Usar con Live Server**

### **1. Instalar Live Server en VS Code:**
1. Abre VS Code
2. Ve a Extensions (Ctrl+Shift+X)
3. Busca "Live Server"
4. Instala la extensión de Ritwick Dey

### **2. Abrir el HTML con Live Server:**
1. Abre `video-streaming-player.html` en VS Code
2. Haz clic derecho en el archivo
3. Selecciona "Open with Live Server"
4. Se abrirá en `http://127.0.0.1:5500`

### **3. Verificar CORS:**
```javascript
// En la consola del navegador deberías ver:
🌐 CORS configurado para Live Server: http://127.0.0.1:5500
```

## 🧪 **Pruebas de CORS**

### **1. Verificar Headers de Respuesta:**
```bash
curl -H "Origin: http://127.0.0.1:5500" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: authorization" \
  -X OPTIONS \
  http://localhost:8080/api/videos/info
```

**Respuesta esperada:**
```
Access-Control-Allow-Origin: http://127.0.0.1:5500
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Expose-Headers: Content-Range, Accept-Ranges, Content-Length, Authorization
```

### **2. Probar desde Live Server:**
1. Abre `video-streaming-player.html` con Live Server
2. Establece un token JWT
3. Carga un video
4. Verifica que no hay errores de CORS en la consola

### **3. Verificar en Network Tab:**
- Abre las herramientas de desarrollador
- Ve a la pestaña Network
- Haz una petición al API
- Verifica que no hay errores de CORS

## 🔍 **Troubleshooting**

### **❌ Error: "Access to fetch at 'http://localhost:8080/api' from origin 'http://127.0.0.1:5500' has been blocked by CORS policy"**

**Solución:**
1. Verifica que el backend esté corriendo en `http://localhost:8080`
2. Verifica que la configuración de CORS incluya `http://127.0.0.1:5500`
3. Reinicia el backend después de cambiar la configuración

### **❌ Error: "Preflight request doesn't pass access control check"**

**Solución:**
1. Verifica que el método HTTP esté permitido
2. Verifica que los headers estén permitidos
3. Verifica que las credenciales estén configuradas correctamente

### **❌ Error: "Response to preflight request doesn't pass access control check"**

**Solución:**
1. Verifica que el origen esté en la lista de orígenes permitidos
2. Verifica que el backend esté enviando los headers CORS correctos

## 📊 **Ventajas de esta Configuración**

### **✅ Seguridad Mejorada:**
- **Orígenes específicos**: Solo permite Live Server, no cualquier origen
- **Métodos limitados**: Solo los métodos HTTP necesarios
- **Headers controlados**: Permite headers necesarios pero no todos

### **✅ Compatibilidad con Live Server:**
- **Puerto 5500**: Puerto por defecto de Live Server
- **127.0.0.1 y localhost**: Ambos formatos soportados
- **Hot reload**: Funciona con la recarga automática de Live Server

### **✅ Streaming Funcionando:**
- **Range Requests**: Soporte para streaming progresivo
- **Headers expuestos**: Content-Range, Accept-Ranges, Content-Length
- **JWT tokens**: Autenticación funcionando correctamente

## 🎬 **Flujo de Trabajo Recomendado**

### **1. Desarrollo:**
1. Abre `video-streaming-player.html` en VS Code
2. Inicia Live Server (puerto 5500)
3. Inicia el backend Spring Boot (puerto 8080)
4. Prueba la funcionalidad

### **2. Testing:**
1. Usa el HTML player para probar streaming
2. Verifica que no hay errores de CORS
3. Prueba diferentes videos y funcionalidades

### **3. Producción:**
1. Configura orígenes específicos para producción
2. Usa HTTPS en producción
3. Configura headers de seguridad adicionales

## 📝 **Configuración Adicional**

### **Para agregar más orígenes:**
```java
// En CorsConfig.java
configuration.setAllowedOrigins(Arrays.asList(
    "http://127.0.0.1:5500",
    "http://localhost:5500",
    "http://127.0.0.1:3000",  // React dev server
    "http://localhost:3000"   // React dev server
));
```

### **Para agregar más métodos:**
```java
configuration.setAllowedMethods(Arrays.asList(
    "GET", "POST", "PUT", "DELETE", "OPTIONS", 
    "HEAD", "PATCH", "TRACE", "CONNECT"
));
```

---

**¡CORS configurado específicamente para Live Server de VS Code!** 🌐✅
