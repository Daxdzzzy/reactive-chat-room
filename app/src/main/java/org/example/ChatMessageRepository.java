package org.example;

import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public class ChatMessageRepository {
    
    private final ConnectionFactory connectionFactory;
    
    public ChatMessageRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    
    public Mono<ChatMessage> save(ChatMessage message) {
        String sql = "INSERT INTO chat_messages (user_id, message_text, timestamp) VALUES ($1, $2, $3) RETURNING id";
        
        return Mono.from(connectionFactory.create())
            .flatMap(connection -> 
                Mono.from(connection.createStatement(sql)
                    .bind("$1", message.getUserId())
                    .bind("$2", message.getMessageText())
                    .bind("$3", message.getTimestamp())
                    .execute())
                .flatMap(result -> Mono.from(result.map((row, metadata) -> 
                    row.get("id", Long.class))))
                .doOnSuccess(id -> message.setId(id))
                .thenReturn(message)
                .doFinally(signalType -> connection.close())
            );
    }

    public Flux<ChatMessage> findAll() {
        String sql = "SELECT cm.id, cm.user_id, u.username, cm.message_text, cm.timestamp " +
                     "FROM chat_messages cm " +
                     "JOIN users u ON cm.user_id = u.id " +
                     "ORDER BY cm.timestamp ASC";
        
        return Flux.from(connectionFactory.create())
            .flatMap(connection -> 
                Flux.from(connection.createStatement(sql).execute())
                    .flatMap(result -> result.map((row, metadata) -> 
                        new ChatMessage(
                            row.get("id", Long.class),
                            row.get("user_id", Long.class),
                            row.get("username", String.class),
                            row.get("message_text", String.class),
                            row.get("timestamp", java.time.LocalDateTime.class)
                        )
                    ))
                    .doFinally(signalType -> connection.close())
            );
    }
}
