package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import it.unipi.lsmsdb.bookadvisor.model.user.*;
import it.unipi.lsmsdb.bookadvisor.utils.HashingUtility;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static final String COLLECTION_NAME = "users";
    private MongoCollection<Document> collection;

    public UserDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        collection = database.getCollection(COLLECTION_NAME);
    }

    // Insert user into MongoDB
    public void insertUser(User user) {
        try {
            String hashedPassword = HashingUtility.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
            collection.insertOne(user.toDocument());
        } catch (Exception e) {
            System.err.println("Errore durante l'inserimento dell'utente: " + e.getMessage());
        }
    }

    // Find a user by their ID
    public User findUserById(String id) {
        try {
            Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            return createUserFromDocument(doc);
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca dell'utente per ID: " + e.getMessage());
            return null;
        }
    }

    // Find a user by their username
    public User findUserByUsername(String username) {
        try {
            Document doc = collection.find(Filters.eq("username", username)).first();
            return createUserFromDocument(doc);
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca dell'utente per username: " + e.getMessage());
            return null;
        }
    }

    // Update a user's information
    public boolean updateUser(String id, User user) {
        try {
            String hashedPassword = HashingUtility.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);
            UpdateResult result = collection.updateOne(Filters.eq("_id", new ObjectId(id)), 
                                                      new Document("$set", user.toDocument()));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento dell'utente: " + e.getMessage());
            return false;
        }
    }

    // Delete a user from the database
    public boolean deleteUser(String id) {
        try {
            DeleteResult result = collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Errore durante la cancellazione dell'utente: " + e.getMessage());
            return false;
        }
    }

    // List all users
    public List<User> listAllUsers() {
        List<User> users = new ArrayList<>();
        try {
            for (Document doc : collection.find()) {
                users.add(createUserFromDocument(doc));
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'elenco di tutti gli utenti: " + e.getMessage());
        }
        return users;
    }

    // Helper method to create a User object from a MongoDB document
    private User createUserFromDocument(Document doc) {
        if (doc == null) {
            return null;
        }

        // Verifica se è un Admin
        if (doc.containsKey("isAdmin") && doc.getBoolean("isAdmin")) {
            return new Admin(doc);
        }

        // Verifica se è un Author
        if (doc.containsKey("genres")) {
            return new Author(doc);
        }

        // Se non è né Admin né Author, allora è un RegisteredUser
        return new RegisteredUser(doc);
    }

}
