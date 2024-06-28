package it.unipi.lsmsdb.bookadvisor.model.user;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class Author extends Reviewer {
    private List<String> genres;
    private List<ObjectId> bookIds; // New attribute for book IDs

    // Default constructor
    public Author() {
        super();
    }

    // Parameterized constructor
    public Author(ObjectId id, String name, String nickname, String password, LocalDate birthdate,
                  String gender, String nationality, List<String> favouriteGenres, List<String> spokenLanguages, 
                  List<String> genres, List<ObjectId> bookIds) {
        super(id, name, nickname, password, birthdate, gender, nationality, favouriteGenres, spokenLanguages);
        this.genres = genres;
        this.bookIds = bookIds; // Initialize bookIds
    }
    
    public Author(String name, String nickname, String password, LocalDate birthdate,
                  String gender, String nationality, List<String> favouriteGenres, List<String> spokenLanguages, 
                  List<String> genres) {
        super(name, nickname, password, birthdate, gender, nationality, favouriteGenres, spokenLanguages);
        this.genres = genres;
        this.bookIds = null; // Initialize bookIds
    }

    // Constructor from MongoDB Document
    public Author(Document doc) {
        super(doc);
        this.genres = doc.getList("genres", String.class);
        this.bookIds = doc.getList("bookIds", ObjectId.class); // Extract bookIds
    }

    // Constructor from Neo4j Node
    public Author(org.neo4j.driver.types.Node node) {
        super(node);
        this.genres = null;
        this.bookIds = null; // Initialize bookIds
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

    // Getter and setter for bookIds
    public List<ObjectId> getBookIds() {
        return bookIds;
    }

    public void setBookIds(List<ObjectId> bookIds) {
        this.bookIds = bookIds;
    }

    // toString method for debugging and logging
    @Override
    public String toString() {
        return "Author{" +
                "genres=" + genres +
                ", bookIds=" + bookIds +
                "} " + super.toString();
    }

    // Convert to MongoDB Document
    @Override
    public Document toDocument() {
        Document doc = super.toDocument();
        doc.append("genres", genres)
           .append("bookIds", bookIds); // Append bookIds
        return doc;
    }
    
    // CRUD operations for bookIds

    // Add a book ID to the bookIds list
    public void addBook(ObjectId bookId) {
        if (this.bookIds == null) {
            this.bookIds = new ArrayList<>();
        }
        this.bookIds.add(bookId);
    }

    // Remove a book ID from the bookIds list
    public void removeBook(ObjectId bookId) {
        if (this.bookIds != null) {
            this.bookIds.remove(bookId);
        }
    }

    // Update a book ID in the bookIds list
    public void updateBook(ObjectId oldBookId, ObjectId newBookId) {
        if (this.bookIds != null) {
            int index = this.bookIds.indexOf(oldBookId);
            if (index != -1) {
                this.bookIds.set(index, newBookId);
            }
        }
    }

    // Get all book IDs
    public List<ObjectId> getAllBooks() {
        return this.bookIds;
    }
}
