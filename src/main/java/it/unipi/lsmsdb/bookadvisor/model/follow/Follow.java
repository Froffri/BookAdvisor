package it.unipi.lsmsdb.bookadvisor.model.follow;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Follow {

    private ObjectId followerId;
    private ObjectId followedId;

    // Constructor
    public Follow(ObjectId followerId, ObjectId followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
    }

    // Getters and Setters
    public ObjectId getFollowerId() {
        return followerId;
    }

    public void setFollowerId(ObjectId followerId) {
        this.followerId = followerId;
    }

    public ObjectId getFollowedId() {
        return followedId;
    }

    public void setFollowedId(ObjectId followedId) {
        this.followedId = followedId;
    }

    // Method to convert to a database Document if using MongoDB
    public Document toDocument() {
        Document doc = new Document();
        doc.append("followerId", this.followerId);
        doc.append("followedId", this.followedId);
        return doc;
    }

    // ToString method for debugging
    @Override
    public String toString() {
        return "Follow{" +
                "followerId=" + followerId +
                ", followedId=" + followedId +
                '}';
    }
}
