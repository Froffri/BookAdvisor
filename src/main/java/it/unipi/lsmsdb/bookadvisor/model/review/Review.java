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

    // Constructor
    public Review(ObjectId id, ObjectId userId, ObjectId bookId, String nickname, String text, String country, int stars, int countUpVote, int countDownVote) {
        this.id = id;
        this.userId = userId;
        this.bookId = bookId;
        this.nickname = nickname; 
        this.text = text;
        this.country = country;
        this.stars = stars;
        this.countUpVote = countUpVote;
        this.countDownVote = countDownVote;
    }

    // Constructor that accepts a Document
    public Review(Document doc) {
        this.id = doc.getObjectId("_id");
        this.userId = doc.getObjectId("userId");
        this.bookId = doc.getObjectId("bookId");
        this.nickname = doc.getString("nickname"); 
        this.text = doc.getString("text");
        this.country = doc.getString("country");
        this.stars = doc.getInteger("stars", 0);
        this.countUpVote = doc.getInteger("countUpVote", 0);
        this.countDownVote = doc.getInteger("countDownVote", 0);
    }

    // Constructor that accepts a Node
    public Review(Node node) {
        this.id = null;
        this.userId = new ObjectId(node.get("userId").asString());
        this.bookId = new ObjectId(node.get("bookId").asString());
        this.nickname = null;
        this.text = null;
        this.country = null;
        this.stars = node.get("stars").asInt();
        this.countUpVote = 0;
        this.countDownVote = 0;
    }

    // Method to convert to Document
    public Document toDocument() {
        return new Document("userId", userId)
                .append("bookId", bookId)
                .append("nickname", nickname)
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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    // toString method for debugging and logging
    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", userId=" + userId +
                ", bookId=" + bookId +
                ", nickname='" + nickname + '\'' +
                ", text='" + text + '\'' +
                ", country='" + country + '\'' +
                ", stars=" + stars +
                ", countUpVote=" + countUpVote +
                ", countDownVote=" + countDownVote +
                '}';
    }
}
