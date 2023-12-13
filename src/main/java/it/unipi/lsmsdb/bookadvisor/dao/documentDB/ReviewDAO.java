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
    private BookDao bookDao;

    public ReviewDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        collection = database.getCollection(COLLECTION_NAME);
        this.bookDao = new BookDao(connector);
    }

    // Insert a new review into the database
    public void addReview(Review review) {
        try {
            collection.insertOne(review.toDocument());
            bookDao.updateBookRating(review.getBookId(), review.getStars());
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiunta della recensione: " + e.getMessage());
        }
    }

    // Update a review's information
    public boolean updateReview(Review review) {
        try {
            // Trova la recensione vecchia prima dell'aggiornamento
            Review oldReview = findReviewById(review.getId());
    
            // Aggiorna la recensione nel database
            UpdateResult result = collection.updateOne(Filters.eq("_id", review.getId()), new Document("$set", review.toDocument()));
    
            // Se la recensione è stata effettivamente aggiornata
            if (result.getModifiedCount() > 0) {
                // Sottrai il valore vecchio prima di aggiungere il nuovo valore
                bookDao.updateBookRating(review.getBookId(), -oldReview.getStars());
                bookDao.updateBookRating(review.getBookId(), review.getStars());
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento della recensione: " + e.getMessage());
            return false;
        }
    }    

    // Delete a review from the database
    public boolean deleteReview(ObjectId id) {
        try {
            DeleteResult result = collection.deleteOne(Filters.eq("_id", id));
            return result.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("Errore durante la cancellazione della recensione: " + e.getMessage());
            return false;
        }
    }

    // Find a review by its ID
    public Review findReviewById(ObjectId id) {
        try {
            Document doc = collection.find(Filters.eq("_id", id)).first();
            return doc != null ? new Review(doc) : null;
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca della recensione per ID: " + e.getMessage());
            return null;
        }
    }

    // Find reviews by book ID
    public List<Review> findReviewsByBookId(ObjectId bookId) {
        List<Review> reviews = new ArrayList<>();
        try {
            for (Document doc : collection.find(Filters.eq("bookId", bookId))) {
                reviews.add(new Review(doc));
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca delle recensioni per ID libro: " + e.getMessage());
        }
        return reviews;
    }
}
