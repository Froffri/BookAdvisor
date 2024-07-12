package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.model.user.Author;
import it.unipi.lsmsdb.bookadvisor.model.user.Reviewer;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.UserDao;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.UserGraphDAO;
import it.unipi.lsmsdb.bookadvisor.utils.*;
import java.time.LocalDate;
import java.util.List;

public class AuthenticationService {
    private UserDao userDao;
    private UserGraphDAO userGraphDAO;

    public AuthenticationService(UserDao userDao, UserGraphDAO userGraphDAO) {
        this.userDao = userDao;
        this.userGraphDAO = userGraphDAO;
    }

    public boolean signUp(String username, String password, String name, String gender, LocalDate birthdate, String nationality, List<String> favouriteGenres, List<String> spokenLanguages, List<String> genres) {
        // Verifica che la password soddisfi i criteri stabiliti
        if (!PasswordValidator.newPasswordMeetsCriteria(password)) {
            // La password non soddisfa i criteri
            System.out.println("Password does not meet criteria");
            return false;
        }

        // Verifica se l'utente esiste già
        if (userDao.findUserByUsername(username) != null) {
            // L'utente esiste già
            System.out.println("User already exists");
            return false;
        }

        // Hash della password
        String hashedPassword = HashingUtility.hashPassword(password);

        // Creazione di un nuovo utente
        if (genres != null) {
            Author newUser = new Author(name, username, hashedPassword, birthdate, gender, nationality, favouriteGenres, spokenLanguages, genres);
            
            // Aggiunta dell'utente al database
            if (userDao.addUser(newUser)) {
                // Insertion in document successful
                if (userGraphDAO.addUser(userDao.findUserByUsername(username).getId(), newUser)) {
                    // Insertion in graph successful
                    return true;
                } else {
                    // Insertion in graph failed
                    System.out.println("Failed to insert user in graph");
                    userDao.deleteUser(newUser.getId());
                    return false;
                }
            } else {
                // Insertion in document failed
                System.out.println("Failed to insert author in document database");
                return false;
            }
        } else if (favouriteGenres != null) {
            Reviewer newUser = new Reviewer(name, username, hashedPassword, birthdate, gender, nationality, favouriteGenres, spokenLanguages); 
            
            // Aggiunta dell'utente al database
            if (userDao.addUser(newUser)) {
                // Insertion in document successful
                if (userGraphDAO.addUser(userDao.findUserByUsername(username).getId(), newUser)) {
                    // Insertion in graph successful
                    return true;
                } else {
                    // Insertion in graph failed
                    System.out.println("Failed to insert reviewer in graph");
                    userDao.deleteUser(newUser.getId());
                    return false;
                }
            } else {
                // Insertion in document failed
                System.out.println("Failed to insert reviewer in document database");
                return false;
            }
        } else {
            // Invalid user type
            System.out.println("Invalid user type");
            return false;
        }
    }

    public Reviewer logIn(String username, String password) {
        Reviewer user = userDao.findUserByUsername(username);
        if (user != null && HashingUtility.checkPassword(password, user.getPassword())) {
            // Successful login
            return user;
        }
        // Login failed
        System.out.println("Login failed for user: " + username);
        return null;
    }
}
