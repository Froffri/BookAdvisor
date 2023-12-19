package it.unipi.lsmsdb.bookadvisor.model.vote;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Vote {

    private ObjectId userId;
    private ObjectId reviewId;
    private boolean upVote; // true for upvote, false for downvote

    // Constructor
    public Vote(ObjectId userId, ObjectId reviewId, boolean upVote) {
        this.userId = userId;
        this.reviewId = reviewId;
        this.upVote = upVote;
    }

    // Getters and Setters
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

