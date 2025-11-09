package org.example;

import reactor.core.publisher.Sinks;
import reactor.netty.http.server.HttpServer;
import java.nio.file.Path;
import reactor.core.publisher.Flux;
import io.r2dbc.spi.ConnectionFactory;

public class ChatServer {
    
    private static final Sinks.Many<String> chatSink = 
        Sinks.many().multicast().onBackpressureBuffer();

    private static final ConnectionFactory connectionFactory = 
        DatabaseConfig.createConnectionFactory();

    private static final ChatMessageRepository repository = 
        new ChatMessageRepository(connectionFactory);

    private static final UserRepository userRepository = 
        new UserRepository(connectionFactory);

    public static void main(String[] args) {
            HttpServer.create()
            .port(Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")))
            .route(routes -> routes
            .ws("/chat", (in, out) -> {
              Flux<String> history = repository.findAll()
                  .map(msg -> msg.getUsername() + ": " + msg.getMessageText());

              in.receive()
              .asString()
              .flatMap(message -> {
                  String[] parts = message.split(":", 2);
                  String username = parts.length > 1 ? parts[0].trim() : "Anonymous";
                  String messageText = parts.length > 1 ? parts[1].trim() : message;
                  
                  return userRepository.findOrCreateUser(username)
                      .flatMap(userId -> {
                          ChatMessage chatMessage = new ChatMessage(username, messageText);
                          chatMessage.setUserId(userId);
                          
                          return repository.save(chatMessage)
                              .doOnSuccess(saved -> chatSink.tryEmitNext(message));
                      });
              })
              .subscribe();

              return out.sendString(Flux.concat(history, chatSink.asFlux()));
            })
              .get("/chat.html", (req, res) -> 
                res.header("Content-Type", "text/html; charset=UTF-8")
                .sendFile(Path.of("src/main/resources/static/chat.html"))
                )
              .get("/", (req, res) -> res.sendRedirect("/chat.html"))
            )
            .bindNow()
            .onDispose()
            .block();
    }
}
