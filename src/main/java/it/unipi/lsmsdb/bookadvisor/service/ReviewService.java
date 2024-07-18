package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.BookDao;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.ReviewDao;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.UserDao;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.ReviewGraphDAO;
import it.unipi.lsmsdb.bookadvisor.model.review.Review;
import it.unipi.lsmsdb.bookadvisor.model.user.*;
import org.bson.types.ObjectId;

import java.util.List;

public class ReviewService {
    private ReviewDao reviewDao;
    private ReviewGraphDAO reviewGraphDao;
    private BookDao bookDao;
    private UserDao userDao;

    public ReviewService(ReviewDao reviewDao, ReviewGraphDAO reviewGraphDAO, BookDao bookDao, UserDao userDao) {
        this.reviewDao = reviewDao;
        this.bookDao = bookDao;
        this.reviewGraphDao = reviewGraphDAO;
        this.userDao = userDao;
    }

    // Aggiungi una nuova recensione al database
    public boolean addReview(Review review) {
        // Validate the rating
        validateStars(review.getStars());

        // Check if the review already exists
        if (reviewDao.findReviewByUserIdAndBookId(review.getUserId(), review.getBookId()) != null) {
            System.err.println("Recensione già presente.");
            return false;
        }

        if(reviewDao.addReview(review.getId(), review)){
            if(bookDao.addReviewToBook(review.getBookId(), review.getId())){
                if(bookDao.updateBookRating(review.getBookId(), review.getStars(), review.getCountry())){
                    // Successfully added the review to mongodb
                    if(reviewGraphDao.addReview(review)){
                        // Successfully added the review to neo4j
                        return true;
                    } else {
                        // Failed to add the review to neo4j
                        System.out.println("Failed to add review to graph");
                        reviewDao.deleteReview(review.getId());
                        return false;
                    }
                } else {
                    // Failed to add the review to mongodb
                    System.out.println("Failed to add review to book");
                    reviewDao.deleteReviewById(review.getId());
                    bookDao.removeReviewFromBook(review.getBookId(), review.getId());
                    return false;
                }
            } else {
                // Failed to add the review to mongodb
                System.out.println("Failed to add review to book");
                reviewDao.deleteReviewById(review.getId());
                return false;
            }
        }
        return false;
    }

    // Aggiorna le informazioni di una recensione
    public boolean updateReview(Review updatedReview, Reviewer currentUser) {
        // Validate the rating
        validateStars(updatedReview.getStars());

        // Retrieve the existing review
        Review existingReview = reviewDao.findReviewById(updatedReview.getId());

        if (existingReview == null) {
            System.err.println("Recensione non trovata.");
            return false;
        }

        // Check if the current user is the author of the review
        if (existingReview.getUserId().equals(currentUser.getId())) {
            if(reviewDao.updateReview(updatedReview)){
                // Successfully updated the review in mongodb
                if(reviewGraphDao.updateReview(updatedReview)){
                    // Successfully updated the review in neo4j
                    return true;
                } else {
                    // Failed to update the review in neo4j
                    System.out.println("Failed to update review in graph");
                    reviewDao.updateReview(existingReview);
                    return false;
                }
            }
            return false;
        } else {
            System.err.println("L'utente non ha i permessi per modificare questa recensione.");
            return false;
        }
    }

    // Elimina una recensione dal database
    public boolean deleteReview(ObjectId reviewId, Reviewer currentUser, ObjectId bookId) {
        Review review = reviewDao.findReviewById(reviewId);

        if (review == null) {
            System.err.println("Recensione non trovata.");
            return false;
        }

        // Check if the current user is the author of the review or an admin
        if (currentUser instanceof Admin || review.getUserId().equals(currentUser.getId())) {

            if(reviewDao.deleteReview(reviewId)){
                if(bookDao.removeReviewFromBook(bookId, reviewId)){
                    // Successfully deleted the review from mongodb
                    if(reviewGraphDao.deleteReview(review)){
                        // Successfully deleted the review from neo4j
                        return true;
                    } else {
                        // Failed to delete the review from neo4j
                        System.out.println("Failed to delete review from graph");
                        reviewDao.addReview(review.getId(), review);
                        bookDao.addReviewToBook(bookId, reviewId);

                        // Rollback the deletion from mongodb
                        bookDao.updateBookRating(bookId, review.getStars(), review.getCountry());

                        return false;
                    }
                } else {
                    // Failed to delete the review from mongodb
                    System.out.println("Failed to delete review from book");
                    reviewDao.addReview(review.getId(), review);
                    return false;
                }
            }
            return false;
        } else {
            System.err.println("L'utente non ha i permessi per eliminare questa recensione.");
            return false;
        }
    }

    // Trova una recensione per ID
    public Review findReviewById(ObjectId id) {
        return reviewDao.findReviewById(id);
    }

    // Trova recensioni per ID libro
    public List<Review> findReviewsByBookId(ObjectId bookId) {
        return reviewDao.findReviewsByBookId(bookId);
    }

    // Trova recensioni per ID utente
    public List<Review> findReviewsByUserId(ObjectId userId) {
        return reviewDao.findReviewsByUserId(userId);
    }

    // Trova recensione per ID utente e ID libro
    public Review findReviewByUserIdAndBookId(ObjectId userId, ObjectId bookId) {
        return reviewDao.findReviewByUserIdAndBookId(userId, bookId);
    }

    // Trova recensioni per username
    public List<Review> findReviewsByUsername(String username) {
        return reviewDao.findReviewsByUsername(username);
    }

    // Trova recensione per username e nome libro
    public Review findReviewByUsernameAndBookName(String username, String bookName) {
        return reviewDao.findReviewByUsernameAndBookName(username, bookName);
    }

    // Trova recensioni per numero di stelle
    public List<Review> findReviewsByStars(int stars) {
        validateStars(stars);
        return reviewDao.findReviewsByStars(stars);
    }

    // Trova recensioni per numero di stelle e nome libro
    public List<Review> findReviewsByStarsAndBookName(int stars, String bookName) {
        validateStars(stars);
        return reviewDao.findReviewsByStarsAndBookName(stars, bookName);
    }

    // Trova recensioni per numero di stelle e username
    public List<Review> findReviewsByStarsAndUsername(int stars, String username) {
        validateStars(stars);
        return reviewDao.findReviewsByStarsAndUsername(stars, username);
    }

    // Metodo privato per la validazione del numero di stelle
    private void validateStars(int stars) {
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Il numero di stelle deve essere compreso tra 1 e 5");
        }
    }
}
