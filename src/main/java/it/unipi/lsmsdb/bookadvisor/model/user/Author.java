package it.unipi.lsmsdb.bookadvisor.model.user;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.List;
import java.time.LocalDate;

public class Author extends Reviewer {
    private List<String> genres;

    // Default constructor
    public Author() {
        super();
    }

    // Parameterized constructor
    public Author(ObjectId id, String name, String nickname, String password, LocalDate birthdate,
                  String gender, String nationality, List<String> favouriteGenres, List<String> spokenLanguages, 
                  List<String> genres) {
        super(id, name, nickname, password, birthdate, gender, nationality, favouriteGenres, spokenLanguages);
        this.genres = genres;
    }
    
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

    // Constructor from Neo4j Node
    public Author(org.neo4j.driver.types.Node node) {
        super(node);
        this.genres = null;
    }

    // Getter and setter for genres
    public List<String> getGenres() {
        return genres;
    }

    public String getGenresString() {
        String genres = "[";
        for (String genre : this.genres) {
            genres += genre + ", ";
        }
        return genres.substring(0, genres.length() - 2) + "]";
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
