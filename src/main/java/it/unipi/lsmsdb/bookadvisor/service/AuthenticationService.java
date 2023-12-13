package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.model.user.User;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.UserDao;
import it.unipi.lsmsdb.bookadvisor.utils.HashingUtility;
import java.time.LocalDate;

public class AuthenticationService {
    private UserDao userDao;

    public AuthenticationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean signUp(String username, String password, String name, String gender, LocalDate birthdate) {
        if (userDao.findUserByUsername(username) != null) {
            // User already exists
            return false;
        }
        String hashedPassword = HashingUtility.hashPassword(password);
        User newUser = new User(name, username, hashedPassword, birthdate, gender);
        userDao.addUser(newUser);
        return true;
    }

    public User login(String username, String password) {
        User user = userDao.findUserByUsername(username);
        if (user != null && HashingUtility.checkPassword(password, user.getPassword())) {
            // Successful login
            return user;
        }
        // Login failed
        return null;
    }
}
