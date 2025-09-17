package com.mike.streming.repository;

import com.mike.streming.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad User
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    /**
     * Buscar usuario por username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Buscar usuario por email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Buscar usuario por username o email
     */
    @Query("{'$or': [{'username': ?0}, {'email': ?0}]}")
    Optional<User> findByUsernameOrEmail(String usernameOrEmail);
    
    /**
     * Verificar si existe un usuario con el username dado
     */
    boolean existsByUsername(String username);
    
    /**
     * Verificar si existe un usuario con el email dado
     */
    boolean existsByEmail(String email);
    
    /**
     * Buscar usuarios habilitados
     */
    @Query("{'enabled': true}")
    java.util.List<User> findEnabledUsers();
    
    /**
     * Buscar usuarios por rol
     */
    @Query("{'roles': ?0}")
    java.util.List<User> findByRole(String role);
}
