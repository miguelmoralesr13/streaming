package com.mike.streming.config;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.security.Security;

/**
 * Configuración del proveedor de seguridad BouncyCastle
 * Nota: Para AES/CBC/PKCS5Padding usamos BouncyCastle
 */
@Configuration
public class SecurityProviderConfig {

    @PostConstruct
    public void init() {
        // Registrar BouncyCastle como proveedor de seguridad (para otros algoritmos)
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Bean
    public BouncyCastleProvider bouncyCastleProvider() {
        return new BouncyCastleProvider();
    }
}
