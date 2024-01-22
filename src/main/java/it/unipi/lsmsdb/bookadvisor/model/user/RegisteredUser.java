package it.unipi.lsmsdb.bookadvisor.model.user;

import java.util.List;
import java.time.LocalDate;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.*;

public class RegisteredUser extends User {
    private String nationality;
    private List<String> favouriteGenres;
    private List<String> spokenLanguages;

    // Default constructor
    public RegisteredUser() {
        super();
    }

    // Parameterized constructor
    public RegisteredUser(ObjectId id, String name, String nickname, String password, LocalDate birthdate,
                          String gender, String nationality, List<String> favouriteGenres, List<String> spokenLanguages) {
        super(id, name, nickname, password, birthdate, gender);
        this.nationality = nationality;
        this.favouriteGenres = favouriteGenres;
        this.spokenLanguages = spokenLanguages;
    }

    // Constructor from MongoDB Document
    public RegisteredUser(Document doc) {
        super(doc);
        this.nationality = doc.getString("nationality");
        this.favouriteGenres = doc.getList("favouriteGenres", String.class);
        this.spokenLanguages = doc.getList("spokenLanguages", String.class);
    }   
    
    // Constructor from Neo4j Node
    public RegisteredUser(Node node) {
        super(node);
        this.favouriteGenres = node.get("fav_genres").asList(Value::asString);
        this.spokenLanguages = node.get("spoken_lang").asList(Value::asString);
    }

    // Getters and Setters

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public List<String> getFavouriteGenres() {
        return favouriteGenres;
    }

    public void setFavouriteGenres(List<String> favouriteGenres) {
        this.favouriteGenres = favouriteGenres;
    }

    public List<String> getSpokenLanguages() {
        return spokenLanguages;
    }

    public void setSpokenLanguages(List<String> spokenLanguages) {
        this.spokenLanguages = spokenLanguages;
    }

    // toString method for debugging and logging
    @Override
    public String toString() {
        return "RegisteredUser{" +
                "nationality='" + nationality + '\'' +
                ", favouriteGenres=" + favouriteGenres +
                ", spokenLanguages=" + spokenLanguages +
                "} " + super.toString();
    }

    // toDocument method for MongoDB
    @Override
    public Document toDocument() {
        Document doc = super.toDocument();
        doc.append("nationality", nationality)
        .append("favouriteGenres", favouriteGenres)
        .append("spokenLanguages", spokenLanguages);
        return doc;
    }
}
