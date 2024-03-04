package it.unipi.lsmsdb.bookadvisor.model.follow;

import org.bson.types.ObjectId;
import org.neo4j.driver.types.Node;

public class Follow {
    private ObjectId followerId;
    private ObjectId followedId;

    // Constructor
    public Follow(ObjectId followerId, ObjectId followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
    }

    public Follow(Node fwedNode, Node fwerNode) {
        this.followerId = (ObjectId) fwerNode.get("id").asObject();
        this.followedId = (ObjectId) fwedNode.get("id").asObject();
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

    // ToString method for debugging
    @Override
    public String toString() {
        return "Follow{" +
                "followerId=" + followerId +
                ", followedId=" + followedId +
                '}';
    }

}
