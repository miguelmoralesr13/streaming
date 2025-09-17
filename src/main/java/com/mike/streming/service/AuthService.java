package com.mike.streming.service;

import com.mike.streming.dto.AuthResponse;
import com.mike.streming.dto.LoginRequest;
import com.mike.streming.dto.UserRegistrationRequest;
import com.mike.streming.exception.ValidationException;
import com.mike.streming.model.User;
import com.mike.streming.repository.UserRepository;
import com.mike.streming.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Servicio de autenticación
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    /**
     * Registrar nuevo usuario
     */
    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        log.info("Registering new user: {}", request.getUsername());
        
        // Validar que el usuario no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username is already taken");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email is already in use");
        }
        
        // Crear nuevo usuario
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of("USER"))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getUsername());
        
        // Generar tokens
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtService.generateToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);
        
        // Actualizar refresh token en la base de datos
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(jwtService.extractExpiration(refreshToken).getTime() / 1000));
        userRepository.save(user);
        
        return buildAuthResponse(accessToken, refreshToken, user);
    }
    
    /**
     * Iniciar sesión
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsernameOrEmail());
        
        // Autenticar usuario
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Obtener usuario
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByUsernameOrEmail(userPrincipal.getUsername())
                .orElseThrow(() -> new ValidationException("User not found"));
        
        // Actualizar último login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        
        // Generar tokens
        String accessToken = jwtService.generateToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);
        
        // Actualizar refresh token
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(jwtService.extractExpiration(refreshToken).getTime() / 1000));
        userRepository.save(user);
        
        log.info("User logged in successfully: {}", user.getUsername());
        return buildAuthResponse(accessToken, refreshToken, user);
    }
    
    /**
     * Renovar token
     */
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");
        
        if (!jwtService.isTokenValid(refreshToken, null)) {
            throw new ValidationException("Invalid refresh token");
        }
        
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new ValidationException("User not found"));
        
        if (!refreshToken.equals(user.getRefreshToken()) || 
            user.getRefreshTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Invalid or expired refresh token");
        }
        
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String newAccessToken = jwtService.generateToken(userPrincipal);
        String newRefreshToken = jwtService.generateRefreshToken(userPrincipal);
        
        // Actualizar refresh token
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiry(LocalDateTime.now().plusSeconds(jwtService.extractExpiration(newRefreshToken).getTime() / 1000));
        userRepository.save(user);
        
        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }
    
    /**
     * Cerrar sesión
     */
    @Transactional
    public void logout(String refreshToken) {
        log.info("Logging out user");
        
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() -> new ValidationException("User not found"));
        
        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);
        
        SecurityContextHolder.clearContext();
    }
    
    /**
     * Construir respuesta de autenticación
     */
    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.extractExpiration(accessToken).getTime() - System.currentTimeMillis())
                .expiresAt(jwtService.extractExpiration(accessToken).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .roles(user.getRoles())
                        .lastLogin(user.getLastLogin())
                        .build())
                .build();
    }
}
