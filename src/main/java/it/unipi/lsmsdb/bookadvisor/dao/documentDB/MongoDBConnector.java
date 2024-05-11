package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnector {
    private static MongoDBConnector instance;
    private MongoDatabase database;
    private MongoClient mongoClient;

    // Private constructor to ensure singleton pattern
    private MongoDBConnector() {
        try {
            // Replica Set URI with all nodes and the replica set name
            String uri = "mongodb://10.1.1.20:27020,10.1.1.21:27020,10.1.1.22:27020";
            //String uri = "mongodb://localhost:27017";

            // Configure MongoClientSettings, if necessary
            MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(uri))
                    .retryWrites(true) // Enable retryable writes
                    .readPreference(com.mongodb.ReadPreference.primaryPreferred()) // Read preference
                    .writeConcern(com.mongodb.WriteConcern.MAJORITY); // Write concern

            // Create a MongoClient with the given settings
            mongoClient = MongoClients.create(settingsBuilder.build());

            // Access the database that you want to work with
            database = mongoClient.getDatabase("BookAdvisor");

        } catch (Exception e) {
            // Wrap and rethrow the exception as a runtime exception
            throw new RuntimeException("Error initializing MongoDB connection: " + e.getMessage(), e);
        }
    }

    // Method to get the singleton instance of MongoDBConnector
    public static synchronized MongoDBConnector getInstance() {
        if (instance == null) {
            instance = new MongoDBConnector();
        }
        return instance;
    }

    // Method to get the MongoDB database instance
    public MongoDatabase getDatabase() {
        if (database == null) {
            throw new RuntimeException("MongoDB database not initialized");
        }
        return database;
    }

    // Method to close the MongoClient connection when the application is shutting down
    public void close() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
            }
        } catch (Exception e) {
            // Handle the exception or log it, depending on your application's requirements
            throw new RuntimeException("Error closing MongoDB connection: " + e.getMessage(), e);
        }
    }
}
