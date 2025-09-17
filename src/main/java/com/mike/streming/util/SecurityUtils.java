package com.mike.streming.util;

import com.mike.streming.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utilidades de seguridad
 */
@Slf4j
public class SecurityUtils {
    
    /**
     * Obtener usuario actual autenticado
     */
    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        
        return (UserPrincipal) authentication.getPrincipal();
    }
    
    /**
     * Obtener ID del usuario actual
     */
    public static String getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getId() : null;
    }
    
    /**
     * Obtener username del usuario actual
     */
    public static String getCurrentUsername() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
    
    /**
     * Verificar si el usuario actual es admin
     */
    public static boolean isCurrentUserAdmin() {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            return false;
        }
        
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
    
    /**
     * Verificar si el usuario actual tiene un rol específico
     */
    public static boolean hasRole(String role) {
        UserPrincipal user = getCurrentUser();
        if (user == null) {
            return false;
        }
        
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }
    
    /**
     * Verificar si el usuario actual es el propietario del recurso
     */
    public static boolean isOwner(String resourceOwnerId) {
        String currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(resourceOwnerId);
    }
    
    /**
     * Verificar si el usuario actual puede acceder al recurso (es propietario o admin)
     */
    public static boolean canAccessResource(String resourceOwnerId) {
        return isOwner(resourceOwnerId) || isCurrentUserAdmin();
    }
}
