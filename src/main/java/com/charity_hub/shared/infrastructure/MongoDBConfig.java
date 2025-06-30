package com.charity_hub.shared.infrastructure;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Configuration
public class MongoDBConfig {

    @Value("${spring.data.mongodb.uri:}")
    private String mongoUri;

    @Value("${spring.data.mongodb.host:localhost}")
    private String host;

    @Value("${spring.data.mongodb.port:27017}")
    private int port;

    @Value("${spring.data.mongodb.database:charity_hub}")
    private String database;

    @Value("${spring.data.mongodb.username:}")
    private String username;

    @Value("${spring.data.mongodb.password:}")
    private String password;

    @Value("${spring.data.mongodb.authentication-database:admin}")
    private String authDatabase;

    @Bean
    public MongoDatabase mongoDatabase() {
        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder();

        // Create POJO codec registry
        CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder()
                        .automatic(true)
                        .build())
        );

        settingsBuilder.codecRegistry(pojoCodecRegistry)
                .applyToConnectionPoolSettings(builder ->
                        builder.maxSize(4)
                                .minSize(1)
                                .maxWaitTime(5000, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(5000, TimeUnit.MILLISECONDS)
                                .readTimeout(5000, TimeUnit.MILLISECONDS));

        String databaseName;

        // Use URI if provided, otherwise use individual properties
        if (StringUtils.hasText(mongoUri)) {
            ConnectionString connectionString = new ConnectionString(mongoUri);
            settingsBuilder.applyConnectionString(connectionString);

            databaseName = connectionString.getDatabase();
            if (databaseName == null || databaseName.isEmpty()) {
                databaseName = database;
            }
        } else {
            // Use individual properties for local development
            settingsBuilder.applyToClusterSettings(builder ->
                    builder.hosts(Collections.singletonList(new ServerAddress(host, port))));

            // Add authentication if username is provided
            if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
                MongoCredential credential = MongoCredential.createCredential(
                        username, authDatabase, password.toCharArray());
                settingsBuilder.credential(credential);
            }

            databaseName = database;
        }

        MongoClient mongoClient = MongoClients.create(settingsBuilder.build());
        return mongoClient.getDatabase(databaseName);
    }
}