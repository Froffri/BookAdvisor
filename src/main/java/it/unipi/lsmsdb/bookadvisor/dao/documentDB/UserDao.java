package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import it.unipi.lsmsdb.bookadvisor.model.review.Review;
import it.unipi.lsmsdb.bookadvisor.model.user.*;

import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UserDao {
    private static final String COLLECTION_NAME = "users";
    private MongoCollection<Document> collection;

    public UserDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        collection = database.getCollection(COLLECTION_NAME);
    }

    // Insert user into MongoDB
    public boolean addUser(Reviewer user) {
        try {
            BsonValue insertedId = collection.insertOne(user.toDocument()).getInsertedId();
            ObjectId userid = insertedId.asObjectId().getValue();
            user.setId(userid);
            System.out.println("User inserted with ID: " + userid);
        } catch (Exception e) {
            System.err.println("Errore durante l'inserimento del libro: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean addUser(ObjectId userId, Reviewer user) {
        try {
            // Insert the user with the given ID
            collection.insertOne(user.toDocument().append("_id", userId));
        } catch (Exception e) {
            System.err.println("Errore durante l'inserimento del libro: " + e.getMessage());
            return false;
        }
        return true;
    }

    // Find a user by their ID
    public Reviewer findUserById(ObjectId id) {
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

    // Find users by their username
    public List<Reviewer> findUsersByUsername(String username) {
        List<Reviewer> users = new ArrayList<>();
        try {
            Pattern pattern = Pattern.compile(username, Pattern.CASE_INSENSITIVE);
            FindIterable<Document> documents = collection.find(Filters.regex("nickname", pattern));
            for (Document doc : documents) {
                users.add(createUserFromDocument(doc));
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca degli utenti per username: " + e.getMessage());
        }
        return users;
    }

    // Find a user by their username
    public Reviewer findUserByUsername(String username) {
        try {
            Pattern pattern = Pattern.compile(username, Pattern.CASE_INSENSITIVE);
            Document doc = collection.find(Filters.regex("nickname", pattern)).first();
            return createUserFromDocument(doc);
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca dell'utente per username: " + e.getMessage());
            return null;
        }
    }

    // Update a user's information
    public boolean updateUser(Reviewer user) {
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
    public List<Reviewer> listAllUsers() {
        List<Reviewer> users = new ArrayList<>();
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
    public boolean voteForReview(Reviewer user, ObjectId reviewId, boolean vote, ReviewDao reviewDao) {

        List<ObjectId> upVotedReviews = user.getUpVotedReviews();
        List<ObjectId> downVotedReviews = user.getDownVotedReviews();

        boolean hasUpvoted = false;
        if(upVotedReviews != null && upVotedReviews.contains(reviewId)){
            hasUpvoted = true;
        }
        boolean hasDownvoted = false;
        if(downVotedReviews != null && downVotedReviews.contains(reviewId)){
            hasDownvoted = true;
        }

        try {
            if (vote) {
                if (hasUpvoted) {
                    // Remove existing upvote
                    user.getUpVotedReviews().remove(reviewId);
                    reviewDao.updateVoteCount(reviewId, "count_up_votes", -1);
                } else {
                    // Remove existing downvote if present
                    if (hasDownvoted) {
                        user.getDownVotedReviews().remove(reviewId);
                        reviewDao.updateVoteCount(reviewId, "count_down_votes", -1);
                    }
                    // Add new upvote
                    // check if the list is null
                    if (user.getUpVotedReviews() == null) {
                        user.setUpVotedReviews(new ArrayList<>());
                    }
                    user.getUpVotedReviews().add(reviewId);
                    reviewDao.updateVoteCount(reviewId, "count_up_votes", 1);
                }
            } else {
                if (hasDownvoted) {
                    // Remove existing downvote
                    user.getDownVotedReviews().remove(reviewId);
                    reviewDao.updateVoteCount(reviewId, "count_down_votes", -1);
                } else {
                    // Remove existing upvote if present
                    if (hasUpvoted) {
                        user.getUpVotedReviews().remove(reviewId);
                        reviewDao.updateVoteCount(reviewId, "count_up_votes", -1);
                    }
                    // Add new downvote
                    // check if the list is null
                    if (user.getDownVotedReviews() == null) {
                        user.setDownVotedReviews(new ArrayList<>());
                    }
                    user.getDownVotedReviews().add(reviewId);
                    reviewDao.updateVoteCount(reviewId, "count_down_votes", 1);
                }
            }

            // Update user information in the database
            return updateUser(user);
        } catch (Exception e) {
            System.err.println("Error while voting for a review: " + e.getMessage());
            return false;
        }
    }

    // Helper method to create a User object from a MongoDB document
    private Reviewer createUserFromDocument(Document doc) {
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
