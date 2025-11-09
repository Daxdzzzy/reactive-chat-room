package org.example;
import java.time.LocalDateTime;
public class ChatMessage {
    private Long id;
    private Long userId;
    private String username;
    private String messageText;
    private LocalDateTime timestamp;
    
    public ChatMessage(String username, String messageText) {
        this.username = username;
        this.messageText = messageText;
        this.timestamp = LocalDateTime.now();
    }
    
    public ChatMessage(Long id, Long userId, String username, String messageText, LocalDateTime timestamp) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
