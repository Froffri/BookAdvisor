package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.model.review.Review;
import it.unipi.lsmsdb.bookadvisor.model.user.*;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.UserDao;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.ReviewDao;
import it.unipi.lsmsdb.bookadvisor.utils.*;

import java.util.List;

import org.bson.types.ObjectId;

public class UserService {
    private UserDao userDao;
    private ReviewDao reviewDao;

    public UserService(UserDao userDao, ReviewDao reviewDao) {
        this.userDao = userDao;
        this.reviewDao = reviewDao;
    }

    // Cambiare la password dell'utente
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        User user = userDao.findUserById(new ObjectId(userId));
        if (user != null && HashingUtility.hashPassword(oldPassword).equals(user.getPassword())) {
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

    // Aggiornamento dei dettagli utente
    public boolean updateAccountInformation(String userId, User updatedUser) {
        User existingUser = userDao.findUserById(new ObjectId(userId));
        if (existingUser instanceof Admin || existingUser.getId().equals(updatedUser.getId())) {
            return userDao.updateUser(updatedUser);
        }
        throw new IllegalArgumentException("Non hai i permessi per modificare questo utente.");
    }

    // Metodo per eliminare un account utente
    public boolean deleteAccount(String requestingUserId, String targetUserId) {
        User requestingUser = userDao.findUserById(new ObjectId(requestingUserId));
        if (requestingUser instanceof Admin || requestingUserId.equals(targetUserId)) {
            return userDao.deleteUser(new ObjectId(targetUserId));
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

    public boolean voteForReview(String userId, ObjectId reviewId, int vote) {
        // Check if the user exists and is authorized to vote
        if (isUserAuthorized(userId)) {
            User user = userDao.findUserById(new ObjectId(userId));
            if (user != null) {
                // Fetch the review associated with the reviewId
                Review review = reviewDao.findReviewById(reviewId);
                if (review != null) {
                    // Validate the vote
                    if (isValidVote(user, review)) {
                        // Call the userDao method to vote for the review
                        return userDao.voteForReview(user, reviewId, vote);
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
       
}
