package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import it.unipi.lsmsdb.bookadvisor.model.Review;
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
        Document doc = review.toDocument();
        collection.insertOne(doc);
    }

    // Find a review by its ID
    public Review findReviewById(String id) {
        Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return doc != null ? new Review(doc) : null;
    }

    // Find reviews by book ID
    public List<Review> findReviewsByBookId(String bookId) {
        List<Review> reviews = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("bookId", new ObjectId(bookId)))) {
            reviews.add(new Review(doc));
        }
        return reviews;
    }

    // Update a review's information
    public boolean updateReview(String id, Review review) {
        UpdateResult result = collection.updateOne(Filters.eq("_id", new ObjectId(id)), new Document("$set", review.toDocument()));
        return result.getModifiedCount() > 0;
    }

    // Delete a review from the database
    public boolean deleteReview(String id) {
        DeleteResult result = collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        return result.getDeletedCount() > 0;
    }
}
