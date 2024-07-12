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
        this.userId = doc.getObjectId("user_id");
        this.bookId = doc.getObjectId("book_id");
        this.nickname = doc.getString("nickname"); 
        this.text = doc.getString("review_text");
        this.country = doc.getString("country");
        this.stars = doc.getInteger("rating", 0);
        this.countUpVote = doc.getInteger("count_up_votes", 0);
        this.countDownVote = doc.getInteger("count_down_votes", 0);
    }

    // Constructor that accepts a Node
    public Review(Node node) {
        this.id = null;
        this.userId = new ObjectId(node.get("user_id").asString());
        this.bookId = new ObjectId(node.get("book_id").asString());
        this.nickname = null;
        this.text = null;
        this.country = null;
        this.stars = node.get("rating").asInt();
        this.countUpVote = 0;
        this.countDownVote = 0;
    }

    // Method to convert to Document
    public Document toDocument() {
        return new Document("user_id", userId)
                .append("book_id", bookId)
                .append("nickname", nickname)
                .append("review_text", text)
                .append("country", country)
                .append("rating", stars)
                .append("count_up_votes", countUpVote)
                .append("count_down_votes", countDownVote);
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

    public void incrementUpVote() {
        this.countUpVote++;
    }

    public void decrementUpVote() {
        this.countUpVote--;
    }

    public void incrementDownVote() {
        this.countDownVote++;
    }

    public void decrementDownVote() {
        this.countDownVote--;
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
