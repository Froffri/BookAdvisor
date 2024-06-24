package it.unipi.lsmsdb.bookadvisor;

import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import it.unipi.lsmsdb.bookadvisor.model.user.Author;
import it.unipi.lsmsdb.bookadvisor.model.user.RegisteredUser;
import it.unipi.lsmsdb.bookadvisor.model.user.User;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void testClasses()
    {
        List<String> genres = null;
        String name = "Paolo";
        String username = "Bitta";
        String hashedPassword = "Cacca";
        LocalDate birthdate = null;
        String gender = "Sborra";
        String nationality = "ITA";
        List<String> favouriteGenres = new ArrayList<String>();
        List<String> spokenLanguages = new ArrayList<String>();
        User newUser;
        // Creazione di un nuovo utente
        if(genres != null)
            newUser = new Author(name, username, hashedPassword, birthdate, gender, nationality, favouriteGenres, spokenLanguages, genres);
        else if(favouriteGenres != null)
            newUser = new RegisteredUser(name, username, hashedPassword, birthdate, gender, nationality, favouriteGenres, spokenLanguages); 
        else    
            newUser = new User(name, username, hashedPassword, birthdate, gender);

        System.out.println(newUser.getPassword());
        assertTrue(newUser instanceof RegisteredUser);
    }
}
