package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.model.user.*;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.UserDao;
import it.unipi.lsmsdb.bookadvisor.utils.HashingUtility;
import org.bson.types.ObjectId;

public class UserService {
    private UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    // Cambiare la password dell'utente
    public boolean changePassword(String userId, String newPassword) {
        User user = userDao.findUserById(new ObjectId(userId));
        if (user != null) {
            user.setPassword(HashingUtility.hashPassword(newPassword));
            return userDao.updateUser(user);
        }
        return false;
    }

    // Visualizzazione dei dettagli utente
    public User viewPersonalAccount(String userId) {
        return userDao.findUserById(new ObjectId(userId));
    }

    // Aggiornamento del profilo utente
    public boolean updateAccountInformation(User updatedUser) {
        return userDao.updateUser(updatedUser);
    }

    // Eliminazione del profilo personale
    public boolean deletePersonalAccount(String userId) {
        return userDao.deleteUser(new ObjectId(userId));
    }

    // Logout utente
    public void logout(String userId) {
        // Implementa qui la logica di invalidazione della sessione o del token
    }

    // Altri metodi specifici per il tipo di utente (Admin, Author, RegisteredUser) possono essere aggiunti qui
}
