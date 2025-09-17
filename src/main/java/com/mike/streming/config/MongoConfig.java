package com.mike.streming.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * Configuración personalizada de MongoDB
 */
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    @Value("${spring.data.mongodb.host:localhost}")
    private String host;
    
    @Value("${spring.data.mongodb.port:27017}")
    private int port;
    
    @Value("${spring.data.mongodb.database:video_streaming}")
    private String database;
    
    @Value("${spring.data.mongodb.username:}")
    private String username;
    
    @Value("${spring.data.mongodb.password:}")
    private String password;
    
    @Override
    protected String getDatabaseName() {
        return database;
    }
    
    @Override
    public MongoClient mongoClient() {
        String connectionString;
        
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            connectionString = String.format("mongodb://%s:%s@%s:%d/%s?authSource=admin", 
                username, password, host, port, database);
        } else {
            connectionString = String.format("mongodb://%s:%d/%s", host, port, database);
        }
        
        System.out.println("MongoDB Connection String: " + connectionString);
        
        return MongoClients.create(connectionString);
    }
    
    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}