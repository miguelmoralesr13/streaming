package com.mike.streming.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de encriptación
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "encryption")
public class EncryptionConfig {
    
    private String algorithm;
    private Integer keySize;
    private String masterKey;
    private String transformation;
    
    public EncryptionConfig() {
        this.algorithm = "AES";
        this.keySize = 256;
        this.transformation = "AES/CBC/PKCS5Padding";
    }
}
