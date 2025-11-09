package org.example;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;

public class UserRepository {
    
    private final ConnectionFactory connectionFactory;

    private final Map<String, Long> userIdCache = new ConcurrentHashMap<>();   

    public UserRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    
    // Find or create user by username, return user_id
    public Mono<Long> findOrCreateUser(String username) {
        // Check cache first
        Long cachedId = userIdCache.get(username);
        if (cachedId != null) {
            return Mono.just(cachedId);
        }
        
        // If not in cache, query database
        String selectSql = "SELECT id FROM users WHERE username = $1";
        String insertSql = "INSERT INTO users (username) VALUES ($1) RETURNING id";
        
        return Mono.from(connectionFactory.create())
            .flatMap(connection ->
                Mono.from(connection.createStatement(selectSql)
                    .bind("$1", username)
                    .execute())
                .flatMap(result -> Mono.from(result.map((row, metadata) ->
                    row.get("id", Long.class))))
                .switchIfEmpty(
                    Mono.from(connection.createStatement(insertSql)
                        .bind("$1", username)
                        .execute())
                        .flatMap(result -> Mono.from(result.map((row, metadata) ->
                            row.get("id", Long.class))))
                    )
                    .doFinally(signalType -> connection.close())
                )
                .doOnSuccess(userId -> userIdCache.put(username, userId)); // Cache the result
        }
}
