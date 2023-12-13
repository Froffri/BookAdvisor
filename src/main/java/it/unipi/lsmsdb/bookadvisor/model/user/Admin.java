package it.unipi.lsmsdb.bookadvisor.model.user;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;

public class Admin extends User {
    private boolean isAdmin;

    // Default constructor
    public Admin() {
        super();
        // By default, isAdmin is set to true for Admin objects
        this.isAdmin = true;
    }

    // Parameterized constructor
    public Admin(ObjectId id, String name, String nickname, String password, LocalDate birthdate,
                 String gender, boolean isAdmin) {
        super(id, name, nickname, password, birthdate, gender);
        this.isAdmin = isAdmin;
    }

    // Constructor from MongoDB Document
    public Admin(Document doc) {
        super(doc);
        this.isAdmin = doc.getBoolean("isAdmin");
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
