package com.mike.streming.encryption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Clase para representar datos encriptados
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedData {
    
    private byte[] data;
    private String key;
    private String algorithm;
}
