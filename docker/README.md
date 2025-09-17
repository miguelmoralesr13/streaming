# Docker Configuration

Este directorio contiene los archivos de configuración para Docker.

## Archivos

- `mongo-init.js` - Script de inicialización de MongoDB
- `nginx.conf` - Configuración de nginx para proxy reverso
- `ssl/` - Directorio para certificados SSL (opcional)

## Uso

Para usar estos archivos, ejecuta:

```bash
docker-compose up -d
```

## Configuración de SSL

Para habilitar SSL, coloca tus certificados en el directorio `ssl/`:

- `cert.pem` - Certificado SSL
- `key.pem` - Clave privada SSL

## Usuario Admin por Defecto

El script de inicialización de MongoDB crea un usuario administrador:

- **Username**: admin
- **Email**: admin@videostreaming.com
- **Password**: admin123

**¡IMPORTANTE!** Cambia estas credenciales en producción.
