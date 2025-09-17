package com.mike.streming.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Entidad User para manejo de usuarios del sistema
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    @Field("username")
    private String username;
    
    @Indexed(unique = true)
    @Field("email")
    private String email;
    
    @Field("password")
    private String password;
    
    @Field("roles")
    private Set<String> roles;
    
    @Field("enabled")
    private boolean enabled;
    
    @Field("account_non_expired")
    private boolean accountNonExpired;
    
    @Field("account_non_locked")
    private boolean accountNonLocked;
    
    @Field("credentials_non_expired")
    private boolean credentialsNonExpired;
    
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;
    
    @Field("last_login")
    private LocalDateTime lastLogin;
    
    @Field("refresh_token")
    private String refreshToken;
    
    @Field("refresh_token_expiry")
    private LocalDateTime refreshTokenExpiry;
}
