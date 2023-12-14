package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.ReviewDao;
import it.unipi.lsmsdb.bookadvisor.model.review.Review;
import org.bson.types.ObjectId;

import java.util.List;

public class ReviewService {
    private ReviewDao reviewDao;

    public ReviewService(ReviewDao reviewDao, BookService bookService) {
        this.reviewDao = reviewDao;
    }

    // Aggiungi una nuova recensione al database
    public void addReview(Review review) {
        reviewDao.addReview(review);
    }

    // Aggiorna le informazioni di una recensione
    public boolean updateReview(Review review) {
        return reviewDao.updateReview(review);
    }

    // Elimina una recensione dal database
    public boolean deleteReview(ObjectId id) {
        return reviewDao.deleteReview(id);
    }

    // Trova una recensione per ID
    public Review findReviewById(ObjectId id) {
        return reviewDao.findReviewById(id);
    }

    // Trova recensioni per ID libro
    public List<Review> findReviewsByBookId(ObjectId bookId) {
        return reviewDao.findReviewsByBookId(bookId);
    }
}
