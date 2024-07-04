package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnector {

    private static final String DATABASE_NAME = "BookAdvisor";
    private static MongoDBConnector instance;
    private MongoClient mongoClient;
    private MongoDatabase database;

    // Costruttore privato per prevenire l'istanziazione diretta
    public MongoDBConnector() {
        try {
            ConnectionString connString = new ConnectionString("mongodb://localhost:27017");
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .retryWrites(true)
                    .build();
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(DATABASE_NAME);
        } catch (Exception e) {
            System.err.println("Errore durante la connessione al database: " + e.getMessage());
        }
    }

    // Metodo pubblico statico per ottenere l'istanza
    public static MongoDBConnector getInstance() {
        if (instance == null) {
            synchronized (MongoDBConnector.class) {
                if (instance == null) {
                    instance = new MongoDBConnector();
                }
            }
        }
        return instance;
    }

    // Metodo per ottenere il database
    public MongoDatabase getDatabase() {
        return database;
    }

    // Metodo per chiudere la connessione al database
    // Nota: Considera attentamente quando chiamare questo metodo in un ambiente Singleton
    public void closeConnection() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
            }
        } catch (Exception e) {
            System.err.println("Errore durante la chiusura della connessione al database: " + e.getMessage());
        }
    }
}
