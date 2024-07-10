package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import it.unipi.lsmsdb.bookadvisor.model.review.Review;

import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class ReviewDao {
    private static final String COLLECTION_NAME = "reviews";
    private MongoCollection<Document> collection;
    private BookDao bookDao;
    private UserDao userDao;

    public ReviewDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        collection = database.getCollection(COLLECTION_NAME);
        this.bookDao = new BookDao(connector);
        this.userDao = new UserDao(connector);
    }

    // Insert a new review into the database
    public boolean addReview(Review review) {
        try {
            // get the oid of the inserted review
            BsonValue insertedId = collection.insertOne(review.toDocument()).getInsertedId();
            ObjectId id = insertedId.asObjectId().getValue();
            bookDao.updateBookRating(review.getBookId(), review.getStars(), review.getCountry());
            bookDao.addReviewToBook(review.getBookId(), id);
            userDao.addReview(review.getUserId(), id);
            // Add the review to the object
            review.setId(id);
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiunta della recensione: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean addReview(ObjectId reviewId, Review review) {
        try {
            // Insert the book with the given ID
            collection.insertOne(review.toDocument().append("_id", reviewId));
        } catch (Exception e) {
            System.err.println("Errore durante l'inserimento del libro: " + e.getMessage());
            return false;
        }
        return true;
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
                bookDao.updateBookRating(review.getBookId(), -oldReview.getStars(), oldReview.getCountry());
                bookDao.updateBookRating(review.getBookId(), review.getStars(), review.getCountry());
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
            // Trova la recensione prima dell'eliminazione
            Review deletedReview = findReviewById(id);

            // Elimina la recensione dal database
            DeleteResult result = collection.deleteOne(Filters.eq("_id", id));

            // Se la recensione è stata effettivamente eliminata
            if (result.getDeletedCount() > 0) {
                // Sottrai il punteggio della recensione eliminata dal punteggio totale del libro
                bookDao.updateBookRating(deletedReview.getBookId(), -deletedReview.getStars(), deletedReview.getCountry());
                // Rimuovi la recensione dal libro
                bookDao.removeReviewFromBook(deletedReview.getBookId(), deletedReview.getId());
                userDao.removeReview(deletedReview.getUserId(), deletedReview.getId());
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Errore durante la cancellazione della recensione: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteReviewsByUserId(ObjectId userId) {
        try {
            for (Review review : findReviewsByUserId(userId)) {
                deleteReview(review.getId());
            }
            return true;
        } catch (Exception e) {
            System.err.println("Errore durante la cancellazione delle recensioni per ID utente: " + e.getMessage());
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

    // Find reviews by book ID using the book's review IDs
    public List<Review> findReviewsByBookId(ObjectId bookId) {
        List<Review> reviews = new ArrayList<>();
        try {
            for (ObjectId reviewId : bookDao.findBookById(bookId).getReviewIds()) {
                reviews.add(findReviewById(reviewId));
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca delle recensioni per ID libro: " + e.getMessage());
        }
        return reviews;
    }

    // Find reviews by user ID using document linking of the review ids contained in user
    public List<Review> findReviewsByUserId(ObjectId userId) {
        List<Review> reviews = new ArrayList<>();
        try {
            for (ObjectId reviewId : userDao.findReviewerById(userId).getReviewIds()) {
                reviews.add(findReviewById(reviewId));
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca delle recensioni per ID utente: " + e.getMessage());
        }
        return reviews;
    }

    // Find reviews by user ID and book ID
    public Review findReviewByUserIdAndBookId(ObjectId userId, ObjectId bookId) {
        try {
            Document doc = collection.find(Filters.and(Filters.eq("userId", userId), Filters.eq("bookId", bookId))).first();
            return doc != null ? new Review(doc) : null;
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca della recensione per ID utente e ID libro: " + e.getMessage());
            return null;
        }
    }

    // Find reviews by book's name
    public List<Review> findReviewsByBookName(String bookName) {
        List<Review> reviews = new ArrayList<>();
        try {
            for (Document doc : collection.find(Filters.eq("bookName", bookName))) {
                reviews.add(new Review(doc));
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca delle recensioni per nome libro: " + e.getMessage());
        }
        return reviews;
    }

    // Find reviews by user's username
    public List<Review> findReviewsByUsername(String username) {
        List<Review> reviews = new ArrayList<>();
        try {
            for (Document doc : collection.find(Filters.eq("username", username))) {
                reviews.add(new Review(doc));
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca delle recensioni per username: " + e.getMessage());
        }
        return reviews;
    }

    // Find reviews by user name and book name
    public Review findReviewByUsernameAndBookName(String username, String bookName) {
        try {
            Document doc = collection.find(Filters.and(Filters.eq("username", username), Filters.eq("bookName", bookName))).first();
            return doc != null ? new Review(doc) : null;
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca della recensione per username e nome libro: " + e.getMessage());
            return null;
        }
    }

    // Find reviews by stars
    public List<Review> findReviewsByStars(int stars) {
        List<Review> reviews = new ArrayList<>();
        try {
            for (Document doc : collection.find(Filters.eq("stars", stars))) {
                reviews.add(new Review(doc));
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca delle recensioni per numero di stelle: " + e.getMessage());
        }
        return reviews;
    }

    // Find reviews by stars and book name
    public List<Review> findReviewsByStarsAndBookName(int stars, String bookName) {
        List<Review> reviews = new ArrayList<>();
        try {
            for (Document doc : collection.find(Filters.and(Filters.eq("stars", stars), Filters.eq("bookName", bookName)))) {
                reviews.add(new Review(doc));
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca delle recensioni per numero di stelle e nome libro: " + e.getMessage());
        }
        return reviews;
    }

    // Find reviews by stars and user name
    public List<Review> findReviewsByStarsAndUsername(int stars, String username) {
        List<Review> reviews = new ArrayList<>();
        try {
            for (Document doc : collection.find(Filters.and(Filters.eq("stars", stars), Filters.eq("username", username)))) {
                reviews.add(new Review(doc));
            }
        } catch (Exception e) {
            System.err.println("Errore durante la ricerca delle recensioni per numero di stelle e username: " + e.getMessage());
        }
        return reviews;
    }

    // Update the vote count of a review
    protected void updateVoteCount(ObjectId reviewId, String voteType, int count) {
        try {
            UpdateResult result = collection.updateOne(Filters.eq("_id", reviewId), new Document("$inc", new Document(voteType, count)));
            if (result.getModifiedCount() == 0) {
                System.err.println("Errore: recensione non trovata con ID: " + reviewId);
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento del conteggio dei voti della recensione: " + e.getMessage());
        }
    }
}
