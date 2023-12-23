package it.unipi.lsmsdb.bookadvisor.model.follow;

public class Follow {
    private Long followerId;
    private Long followedId;

    // Constructor for Neo4j
    public Follow(Long followerId, Long followedId) {
        this.followerId = followerId;
        this.followedId = followedId;
    }

    // Getters and Setters
    public Long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

    public Long getFollowedId() {
        return followedId;
    }

    public void setFollowedId(Long followedId) {
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

    // Method to create a follow relationship in Neo4j
    public static void createFollowRelationship(org.neo4j.driver.Driver driver, Long followerId, Long followedId) {
        try (org.neo4j.driver.Session session = driver.session()) {
            String cypherQuery = "MATCH (follower:User), (followed:User) " +
                                 "WHERE ID(follower) = $followerId AND ID(followed) = $followedId " +
                                 "CREATE (follower)-[:FOLLOWS]->(followed)";
            session.run(cypherQuery, org.neo4j.driver.Values.parameters(
                    "followerId", followerId,
                    "followedId", followedId
            ));
        }
    }
}
