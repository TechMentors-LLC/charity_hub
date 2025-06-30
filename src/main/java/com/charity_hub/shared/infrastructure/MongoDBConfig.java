package com.charity_hub.shared.infrastructure;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class MongoDBConfig {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoDatabase mongoDatabase() {
        ConnectionString connectionString = new ConnectionString(mongoUri);

        // Create POJO codec registry
        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder()
                        .automatic(true)
                        .build())
        );

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(pojoCodecRegistry)
                .applyToConnectionPoolSettings(builder ->
                        builder.maxSize(4)
                                .minSize(1)
                                .maxWaitTime(5000, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(5000, TimeUnit.MILLISECONDS)
                                .readTimeout(5000, TimeUnit.MILLISECONDS))
                .build();

        MongoClient mongoClient = MongoClients.create(settings);

        // Get database name from connection string, fallback to default if null
        String databaseName = connectionString.getDatabase();
        if (databaseName == null || databaseName.isEmpty()) {
            databaseName = "charity_hub"; // Default database name
        }

        return mongoClient.getDatabase(databaseName);
    }
}