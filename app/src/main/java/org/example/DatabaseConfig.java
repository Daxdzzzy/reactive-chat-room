package org.example;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;

public class DatabaseConfig {
    
    public static ConnectionFactory createConnectionFactory() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        if (databaseUrl == null) {
            throw new IllegalStateException("DATABASE_URL environment variable not set");
        }
        
        String r2dbcUrl = databaseUrl.replace("postgresql://", "r2dbc:postgresql://");
        
        return ConnectionFactories.get(r2dbcUrl);
    }
}
