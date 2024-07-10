package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.model.review.Review;
import it.unipi.lsmsdb.bookadvisor.model.user.*;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.UserDao;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.UserGraphDAO;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.ReviewDao;
import it.unipi.lsmsdb.bookadvisor.utils.*;

import java.util.List;

import org.bson.types.ObjectId;

public class UserService {
    private UserDao userDao;
    private UserGraphDAO userGraphDao;
    private ReviewDao reviewDao;

    public UserService(UserDao userDao, ReviewDao reviewDao, UserGraphDAO userGraphDao) {
        this.userDao = userDao;
        this.userGraphDao = userGraphDao;
        this.reviewDao = reviewDao;
    }

    // Cambiare la password dell'utente
    public boolean changePassword(ObjectId userId, String newPassword) {
        User user = userDao.findUserById(userId);
        if (user != null) {
            if (PasswordValidator.newPasswordMeetsCriteria(newPassword)) {
                user.setPassword(HashingUtility.hashPassword(newPassword));
                return userDao.updateUser(user);
            }
            throw new IllegalArgumentException("La nuova password non soddisfa i criteri di sicurezza.");
        }
        return false;
    }

    // Visualizzazione dei dettagli utente
    public User viewInfoAccount(String userId) {
        return userDao.findUserById(new ObjectId(userId));
    }

    // CHANGED USER TO REGISTEREDUSER
    // Aggiornamento dei dettagli utente
    public boolean updateAccountInformation(ObjectId userId, User updatedUser) {
        User existingUser = userDao.findUserById(userId);
        if (existingUser instanceof Admin || existingUser.getId().equals(updatedUser.getId())) {
            if(userDao.updateUser(updatedUser)){
                // Successfully updated the user in mongodb
                if(userGraphDao.updateUser(updatedUser)){
                    // Successfully updated the user in neo4j
                    return true;
                } else {
                    // Failed to update the user in neo4j
                    System.out.println("Failed to update the user in neo4j");
                    userDao.updateUser(existingUser);
                    return false;
                }
            }
            return false;
        }
        throw new IllegalArgumentException("Non hai i permessi per modificare questo utente.");
    }

    // Metodo per eliminare un account utente
    public boolean deleteAccount(ObjectId requestingUserId, ObjectId targetUserId) {
        User requestingUser = userDao.findUserById(requestingUserId);
        if (requestingUser instanceof Admin || requestingUserId.equals(targetUserId)) {

            User targetUser = userDao.findUserById(targetUserId);
            List<Review> reviews = userDao.getReviewsByUserId(targetUserId, reviewDao);

            if(reviewDao.deleteReviewsByUserId(targetUserId)){
                if(userDao.deleteUser(targetUserId)){
                    // Successfully deleted the user in mongodb
                    if(userGraphDao.deleteUserById(targetUserId)){
                        // Successfully deleted the user in neo4j
                        return true;
                    } else {
                        // Failed to delete the user in neo4j
                        userDao.addUser(targetUser.getId(), targetUser);
                        for(Review review : reviews){
                            reviewDao.addReview(review);
                        }
                        return false;
                    }
                } else {
                    // Failed to delete the user in mongodb
                    // Restore the user's reviews
                    for(Review review : reviews){
                        reviewDao.addReview(review);
                    }
                    return false;
                }
            } 
            return false;
        }
        throw new IllegalArgumentException("Non hai i permessi per eliminare questo utente.");
    }

    // Visualizzazione della lista degli utenti
    public List<User> listAllUsers() {
        return userDao.listAllUsers();
    }

    // Logout utente
    public boolean logout(String userId) {
        return true;
    }

    // If addVote is true, the vote is added; otherwise, it is removed
    // If vote is true, the vote is an upvote; otherwise, it is a downvote
    public boolean voteForReview(String userId, ObjectId reviewId, boolean vote) {
        // Check if the user exists and is authorized to vote
        if (isUserAuthorized(userId)) {
            Reviewer user = userDao.findReviewerById(new ObjectId(userId));
            if (user != null) {
                // Fetch the review associated with the reviewId
                Review review = reviewDao.findReviewById(reviewId);
                if (review != null) {
                    // Validate the vote
                    if (isValidVote(user, review)) {
                        // Call the userDao method to vote for the review
                        return userDao.voteForReview(user, reviewId, vote, this.reviewDao);
                    } else {
                        System.err.println("Errore: Il voto non è valido.");
                    }
                } else {
                    System.err.println("Errore: Recensione non trovata.");
                }
            } else {
                System.err.println("Errore: Utente non trovato.");
            }
        } else {
            System.err.println("Errore: Utente non autorizzato.");
        }
        return false;
    }
    
    // Additional business logic for validating a vote
    private boolean isValidVote(User user, Review review) {
        return review != null
                && !review.getUserId().equals(user.getId()) // Ensure the vote is not from the same user
                && review.getText() != null && !review.getText().isEmpty(); // Ensure the associated review has non-empty text
    }
    
    // Additional business logic for checking if a user is authorized
    private boolean isUserAuthorized(String userId) {
        // Verifica se l'ID utente è valido e corrisponde a un utente nel sistema
        User user = userDao.findUserById(new ObjectId(userId));
        return user != null;
    }

    public List<User> searchUsersByUsername(String username) {
        return userDao.findUsersByUsername(username);
    }
       
}
