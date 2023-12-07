package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import it.unipi.lsmsdb.bookadvisor.model.review.Review;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class ReviewDao {
    private static final String COLLECTION_NAME = "reviews";
    private MongoCollection<Document> collection;

    public ReviewDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        collection = database.getCollection(COLLECTION_NAME);
    }

    // Insert a new review into the database
    public void insertReview(Review review) {
        try {
            Document doc = review.toDocument();
            collection.insertOne(doc);
        } catch (Exception e) {
            System.err.println("Errore durante l'inserimento della recensione: " + e.getMessage());
        }
    }

    // Find a review by its ID
    public Review findReviewById(String id) {
        try {
            Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            return doc != null ? new Review(doc) : null;
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca della recensione per ID: " + e.getMessage());
            return null;
        }
    }

    // Find reviews by book ID
    public List<Review> findReviewsByBookId(String bookId) {
        List<Review> reviews = new ArrayList<>();
        try {
            for (Document doc : collection.find(Filters.eq("bookId", new ObjectId(bookId)))) {
                reviews.add(new Review(doc));
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca delle recensioni per ID libro: " + e.getMessage());
        }
        return reviews;
    }

    // Update a review's information
    public boolean updateReview(String id, Review review) {
        try {
            UpdateResult result = collection.updateOne(Filters.eq("_id", new ObjectId(id)), new Document("$set", review.toDocument()));
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento della recensione: " + e.getMessage());
            return false;
        }
    }

    // Delete a review from the database
    public boolean deleteReview(String id) {
        try {
            DeleteResult result = collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Errore durante la cancellazione della recensione: " + e.getMessage());
            return false;
        }
    }
}
