package it.unipi.lsmsdb.bookadvisor.model.vote;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Vote {
    private ObjectId id;
    private ObjectId userId;
    private ObjectId reviewId;
    private boolean upVote; // true for upvote, false for downvote

    public Vote(ObjectId id, ObjectId userId, ObjectId reviewId, boolean upVote) {
        this.id = id;
        this.userId = userId;
        this.reviewId = reviewId;
        this.upVote = upVote;
    }

    // Constructor that accepts a Document
    public Vote(Document doc) {
        this.id = doc.getObjectId("_id");
        this.userId = doc.getObjectId("userId");
        this.reviewId = doc.getObjectId("reviewId");
        this.upVote = doc.getBoolean("upVote");
    }

    // Getters and Setters
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

    public ObjectId getReviewId() {
        return reviewId;
    }

    public void setReviewId(ObjectId reviewId) {
        this.reviewId = reviewId;
    }

    public boolean isUpVote() {
        return upVote;
    }

    public void setUpVote(boolean upVote) {
        this.upVote = upVote;
    }

    // Method to toggle the vote
    public void toggleVote() {
        this.upVote = !this.upVote;
    }
    
    // Implement a method to convert to a database Document if using MongoDB
    public Document toDocument() {
        Document document = new Document();
        document.append("userId", userId);
        document.append("reviewId", reviewId);
        document.append("upVote", upVote);
        return document;
    }
    
    // ToString method for debugging
    @Override
    public String toString() {
        return "Vote{" +
                "userId=" + userId +
                ", reviewId=" + reviewId +
                ", upVote=" + upVote +
                '}';
    }
}

