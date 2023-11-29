package it.unipi.lsmsdb.bookadvisor.model;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Review {
    private ObjectId id;
    private ObjectId userId;
    private ObjectId bookId;
    private String text;
    private int stars;

    public Review(ObjectId id, ObjectId userId, ObjectId bookId, String text, int stars) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.text = text;
        this.stars = stars;
    }

    // Costruttore che accetta un Document
    public Review(Document doc) {
        this.id = doc.getObjectId("_id");
        this.userId = doc.getObjectId("userId");
        this.bookId = doc.getObjectId("bookId");
        this.text = doc.getString("text");
        this.stars = doc.getInteger("stars", 0);
    }

    // Metodo per convertire in Document
    public Document toDocument() {
        return new Document("userId", userId)
                .append("bookId", bookId)
                .append("text", text)
                .append("stars", stars);
    }
 
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public ObjectId getBookId() {
        return bookId;
    }

    public void setBookId(ObjectId bookId) {
        this.bookId = bookId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
}
