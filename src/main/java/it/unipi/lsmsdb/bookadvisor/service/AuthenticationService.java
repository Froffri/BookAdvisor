package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.model.user.Author;
import it.unipi.lsmsdb.bookadvisor.model.user.Reviewer;
import it.unipi.lsmsdb.bookadvisor.model.user.User;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.UserDao;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.UserGraphDAO;
import it.unipi.lsmsdb.bookadvisor.utils.*;
import java.time.LocalDate;
import java.util.List;

public class AuthenticationService {
    private UserDao userDao;
    private UserGraphDAO userGraphDAO;

    public AuthenticationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean signUp(String username, String password, String name, String gender, LocalDate birthdate, String nationality, List<String> favouriteGenres, List<String> spokenLanguages, List<String> genres) {
        // Verifica che la password soddisfi i criteri stabiliti
        if (!PasswordValidator.newPasswordMeetsCriteria(password)) {
            // La password non soddisfa i criteri
            return false;
        }

        // Verifica se l'utente esiste già
        if (userDao.findUserByUsername(username) != null) {
            // L'utente esiste già
            return false;
        }

        // Hash della password
        String hashedPassword = HashingUtility.hashPassword(password);

        // Creazione di un nuovo utente
        if(genres != null){
            Author newUser = new Author(name, username, hashedPassword, birthdate, gender, nationality, favouriteGenres, spokenLanguages, genres);
            
            // Aggiunta dell'utente al database
            if(userDao.addUser(newUser)){
                // Insertion in document successful
                if(userGraphDAO.addUser(newUser)){
                    // Insertion in graph successful
                    return true;
                } else {
                    // Insertion in graph failed
                    userDao.deleteUser(newUser.getId());
                    return false;
                }
            }
        }
        else if(favouriteGenres != null){
            Reviewer newUser = new Reviewer(name, username, hashedPassword, birthdate, gender, nationality, favouriteGenres, spokenLanguages); 
            
            // Aggiunta dell'utente al database
            if(userDao.addUser(newUser)){
                // Insertion in document successful
                if(userGraphDAO.addUser(newUser)){
                    // Insertion in graph successful
                    return true;
                } else {
                    // Insertion in graph failed
                    userDao.deleteUser(newUser.getId());
                    return false;
                }
            }
        }
        else{
            User newUser = new User(name, username, hashedPassword, birthdate, gender);

            // Aggiunta dell'utente al database
            if(userDao.addUser(newUser)){
                // Insertion in document successful
                if(userGraphDAO.addUser(newUser)){
                    // Insertion in graph successful
                    return true;
                } else {
                    // Insertion in graph failed
                    userDao.deleteUser(newUser.getId());
                    return false;
                }
            }
        }    
        // Insertion in document failed
        return false;
    }

    public User logIn(String username, String password) {
        User user = userDao.findUserByUsername(username);
        if (user != null && HashingUtility.checkPassword(password, user.getPassword())) {
            // Successful login
            return user;
        }
        // Login failed
        return null;
    }
}
