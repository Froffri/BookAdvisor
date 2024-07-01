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

    // Find a reviewer by their ID
    public Reviewer findReviewerById(ObjectId id) {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            return createReviewerFromDocument(doc);
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca del recensore per ID: " + e.getMessage());
            return null;
        }
    }

    // Find an author by their ID
    public Author findAuthorById(ObjectId id) {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            return createAuthorFromDocument(doc);
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca dell'autore per ID: " + e.getMessage());
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
    // If vote is true, the vote is an upvote; otherwise, it is a downvote
    public boolean voteForReview(Reviewer user, ObjectId reviewId, boolean vote) {

        boolean hasUpvoted = user.getUpVotedReviews().contains(reviewId);
        boolean hasDownvoted = user.getDownVotedReviews().contains(reviewId);

        try {
            if (vote) {
                if (hasUpvoted) {
                    // Remove existing upvote
                    user.getUpVotedReviews().remove(reviewId);
                    reviewDao.updateVoteCount(reviewId, "countUpVote", -1);
                } else {
                    // Remove existing downvote if present
                    if (hasDownvoted) {
                        user.getDownVotedReviews().remove(reviewId);
                        reviewDao.updateVoteCount(reviewId, "countDownVote", -1);
                    }
                    // Add new upvote
                    user.getUpVotedReviews().add(reviewId);
                    reviewDao.updateVoteCount(reviewId, "countUpVote", 1);
                }
            } else {
                if (hasDownvoted) {
                    // Remove existing downvote
                    user.getDownVotedReviews().remove(reviewId);
                    reviewDao.updateVoteCount(reviewId, "countDownVote", -1);
                } else {
                    // Remove existing upvote if present
                    if (hasUpvoted) {
                        user.getUpVotedReviews().remove(reviewId);
                        reviewDao.updateVoteCount(reviewId, "countUpVote", -1);
                    }
                    // Add new downvote
                    user.getDownVotedReviews().add(reviewId);
                    reviewDao.updateVoteCount(reviewId, "countDownVote", 1);
                }
            }

            // Update user information in the database
            return updateUser(user);
        } catch (Exception e) {
            System.err.println("Error while voting for a review: " + e.getMessage());
            return false;
        }
    }


    // Add a review to a user
    public void addReview(ObjectId userId, ObjectId reviewId) {
        Reviewer user = findReviewerById(userId);
        if (user != null) {
            if (user.getReviewIds() == null) {
                user.setReviewIds(new ArrayList<>());
            }
            user.addReview(reviewId);
            updateUser(user);
        }
    }

    // Remove a review from a user
    public void removeReview(ObjectId userId, ObjectId reviewId) {
        Reviewer user = findReviewerById(userId);
        if (user != null) {
            user.removeReview(reviewId);
            updateUser(user);
        }
    }

    // Helper method to create a User object from a MongoDB document
    private User createUserFromDocument(Document doc) {
        if (doc == null) {
            return null;
        }

        // Check if it is an Admin
        if (doc.containsKey("isAdmin") && doc.getBoolean("isAdmin")) {
            return new Admin(doc);
        }

        // Check if it is an Author
        if (doc.containsKey("genres")) {
            return new Author(doc);
        }

        // Otherwise, it is a Reviewer
        return new Reviewer(doc);
    }

    // Helper method to create a Reviewer object from a MongoDB document
    private Reviewer createReviewerFromDocument(Document doc) {
        if (doc == null) {
            return null;
        }

        return new Reviewer(doc);
    }

    // Helper method to create an Author object from a MongoDB document
    private Author createAuthorFromDocument(Document doc) {
        if (doc == null) {
            return null;
        }

        return new Author(doc);
    }
}
