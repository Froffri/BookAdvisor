package it.unipi.lsmsdb.bookadvisor.model.user;

import org.bson.Document;
import java.util.List;
import java.time.LocalDate;

public class Author extends RegisteredUser {
    private List<String> genres;

    // Default constructor
    public Author() {
        super();
    }

    // Parameterized constructor
    public Author(String name, String nickname, String password, LocalDate birthdate,
                  String gender, String nationality, List<String> favouriteGenres, List<String> spokenLanguages, 
                  List<String> genres) {
        super(name, nickname, password, birthdate, gender, nationality, favouriteGenres, spokenLanguages);
        this.genres = genres;
    }

    // Constructor from MongoDB Document
    public Author(Document doc) {
        super(doc);
        this.genres = doc.getList("genres", String.class);
    }

    // Getter and setter for genres
    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    // toString method for debugging and logging
    @Override
    public String toString() {
        return "Author{" +
                "genres=" + genres +
                "} " + super.toString();
    }

    // Convert to MongoDB Document
    @Override
    public Document toDocument() {
        Document doc = super.toDocument();
        doc.append("genres", genres);
        return doc;
    }
}
