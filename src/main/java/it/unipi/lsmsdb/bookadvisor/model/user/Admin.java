package it.unipi.lsmsdb.bookadvisor.model.user;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.List;

public class Admin extends Reviewer {
    private boolean isAdmin;

    // Default constructor
    public Admin() {
        super();
        // By default, isAdmin is set to true for Admin objects
        this.isAdmin = true;
    }

    // Parameterized constructor
    public Admin(ObjectId id, String name, String nickname, String password, LocalDate birthdate,
                 String gender, String nationality, List<String> favouriteGenres, List<String> spokenLanguages, boolean isAdmin) {
        super(id, name, nickname, password, birthdate, gender, nationality, favouriteGenres, spokenLanguages);
        this.isAdmin = isAdmin;
    }

    // Constructor from MongoDB Document
    public Admin(Document doc) {
        super(doc);
        this.isAdmin = doc.getBoolean("isAdmin");
    }    

    // Constructor from Neo4j Node
    public Admin(org.neo4j.driver.types.Node node) {
        super(node);
        this.isAdmin = node.get("isAdmin").asBoolean();
    }

    // Getters and setters for isAdmin
    public boolean isAdmin() {
        return isAdmin;
    }
    
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    // toString method for debugging and logging
    @Override
    public String toString() {
        return "Admin{" +
                "isAdmin=" + isAdmin +
                "} " + super.toString();
    }

    // Convert to MongoDB Document
    @Override
    public Document toDocument() {
        Document doc = super.toDocument();
        doc.append("isAdmin", isAdmin);
        return doc;
    }
}
