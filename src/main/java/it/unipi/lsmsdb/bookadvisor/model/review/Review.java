package it.unipi.lsmsdb.bookadvisor.model.review;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.types.Node;

public class Review {
    private ObjectId id;
    private ObjectId userId;
    private ObjectId bookId;
    private String nickname;
    private String text;
    private String country;
    private int stars;
    private int countUpVote;
    private int countDownVote;

    // Costruttore
    public Review(ObjectId id, ObjectId userId, ObjectId bookId, String text, String country,  int stars, int countUpVote, int countDownVote) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.text = text;
        this.country = country;
        this.stars = stars;
        this.countUpVote = countUpVote;
        this.countDownVote = countDownVote;
    }

    // Costruttore che accetta un Document
    public Review(Document doc) {
        this.id = doc.getObjectId("_id");
        this.userId = doc.getObjectId("userId");
        this.bookId = doc.getObjectId("bookId");
        this.text = doc.getString("text");
        this.country = doc.getString("country");
        this.stars = doc.getInteger("stars", 0);
        this.countUpVote = doc.getInteger("countUpVote", 0);
        this.countDownVote = doc.getInteger("countDownVote", 0);
    }

    // Costruttore che accetta un Node
    public Review(Node node) {
        this.id = null;
        this.userId = new ObjectId(node.get("userId").asString());
        this.bookId = new ObjectId(node.get("bookId").asString());
        this.text = null;
        this.stars = node.get("stars").asInt();
    }

    // Metodo per convertire in Document
    public Document toDocument() {
        return new Document("userId", userId)
                .append("bookId", bookId)
                .append("text", text)
                .append("country", country)
                .append("stars", stars)
                .append("countUpVote", countUpVote)
                .append("countDownVote", countDownVote);
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public int getCountUpVote() {
        return countUpVote;
    }

    public void setCountUpVote(int countUpVote) {
        this.countUpVote = countUpVote;
    }

    public int getCountDownVote() {
        return countDownVote;
    }

    public void setCountDownVote(int countDownVote) {
        this.countDownVote = countDownVote;
    }
}
