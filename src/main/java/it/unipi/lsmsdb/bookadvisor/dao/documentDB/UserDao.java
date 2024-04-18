package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import it.unipi.lsmsdb.bookadvisor.model.user.*;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private static final String COLLECTION_NAME = "users";
    private MongoCollection<Document> collection;
    private ReviewDao reviewDao;

    public UserDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        collection = database.getCollection(COLLECTION_NAME);
    }

    // Insert user into MongoDB
    public boolean addUser(User user) {
        try {
            // Inserimento dell'utente nel database
            collection.insertOne(user.toDocument());
            System.out.println("Inserimento dell'utente riuscito.");
            return true;
        } catch (Exception e) {
            System.err.println("Errore durante l'inserimento dell'utente: " + e.getMessage());
            return false;
        }
    } 

    // Find a user by their ID
    public User findUserById(ObjectId id) {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
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
    public boolean updateUser(User user) {
        try {
            UpdateResult result = collection.updateOne(Filters.eq("_id", user.getId()), 
                                                      new Document("$set", user.toDocument()));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento dell'utente: " + e.getMessage());
            return false;
        }
    }

    // Delete a user from the database
    public boolean deleteUser(ObjectId id) {
        try {
            DeleteResult result = collection.deleteOne(Filters.eq("_id", id));
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

    // Vote for a review (upvote or downvote)
    public boolean voteForReview(User user, ObjectId reviewId, int vote) {
        String voteType = (vote > 0) ? "upvotedReviews" : "downvotedReviews";
        int increment = (vote > 0) ? 1 : -1;

        try {
            UpdateResult result = collection.updateOne(
                Filters.eq("_id", user.getId()),
                new Document(
                    (vote > 0) ? "$addToSet" : "$pull",
                    new Document(voteType, reviewId)
                )
            );

            // Update the vote count in the reviewDao
            reviewDao.updateVoteCount(reviewId, (vote > 0) ? "countupvote" : "countdownvote", increment);

            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Error while voting for a review: " + e.getMessage());
            return false;
        }
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
