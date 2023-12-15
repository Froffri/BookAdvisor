package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.model.user.*;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.UserDao;
import it.unipi.lsmsdb.bookadvisor.utils.*;

import java.util.List;

import org.bson.types.ObjectId;

public class UserService {
    private UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
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
    public User viewPersonalAccount(String userId) {
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
    public void logout(String userId) {
        
    }
}
