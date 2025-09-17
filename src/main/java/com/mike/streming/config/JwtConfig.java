package com.mike.streming.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de JWT
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    private String secret;
    private Long expiration;
    private Long refreshExpiration;
    private String header;
    private String prefix;
    
    public JwtConfig() {
        this.header = "Authorization";
        this.prefix = "Bearer ";
    }
}
