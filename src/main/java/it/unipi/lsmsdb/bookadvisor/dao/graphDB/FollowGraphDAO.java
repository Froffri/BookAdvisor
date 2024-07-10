package it.unipi.lsmsdb.bookadvisor.dao.graphDB;

import static org.neo4j.driver.Values.parameters;

import org.bson.types.ObjectId;
import org.neo4j.driver.*;

import it.unipi.lsmsdb.bookadvisor.model.follow.Follow;
import it.unipi.lsmsdb.bookadvisor.model.user.Reviewer;

public class FollowGraphDAO {
    private final Neo4jConnector connector;

    public FollowGraphDAO(Neo4jConnector connector) {
        this.connector = connector;
    }

    // CREATE

    /**
     * Add a follow relationship between two users
     * @param follow
     */
    public boolean addFollow(Follow follow) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String fwString = follow.getFollowerId().toHexString();
            String fdString = follow.getFollowedId().toHexString();
            session.run(
                "MATCH (fwer:User {id: '$follower'})" +
                "WITH fwer" +
                "MATCH (fwed:User {id: '$followed'})" +
                "WHERE NOT (fwer)-[:FOLLOWS]->(fwed)" +
                "CREATE (fwer)-[:FOLLOWS]->(fwed)", 
                parameters("follower", fwString, 
                            "followed", fdString)
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Add a follow relationship between two users
     * @param follower
     * @param followed
     */
    public boolean addFollow(Reviewer follower, Reviewer followed) {
        // Convert ObjectId to string
        String fwString = follower.getId().toHexString();
        String fdString = followed.getId().toHexString();

        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (fwer:User {id: '$follower'})" +
                "WITH fwer" +
                "MATCH (fwed:User {id: '$followed'}) " +
                "WHERE NOT (fwer)-[:FOLLOWS]->(fwed)" +
                "CREATE (fwer)-[:FOLLOWS]->(fwed)", 
                parameters("follower", fwString, 
                            "followed", fdString)
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Add a follow relationship between two users using their ids
     * @param followerId
     * @param followedId
     */
    public boolean addFollowByIds(ObjectId followerId, ObjectId followedId) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String fwString = followerId.toHexString();
            String fdString = followedId.toHexString();
            session.run(
                "MATCH (fwer:User {id: '$follower'})" +
                "WITH fwer" +
                "MATCH (fwed:User {id: '$followed'}) " +
                "WHERE NOT (fwer)-[:FOLLOWS]->(fwed)" +
                "CREATE (fwer)-[:FOLLOWS]->(fwed)", 
                parameters("follower", fwString, 
                            "followed", fdString)
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // READ 

    public boolean getFollow(Reviewer follower, Reviewer followed) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String fwString = follower.getId().toHexString();
            String fdString = followed.getId().toHexString();
            Result result = session.run(
                "MATCH (fwr:User {id: '$follower'})-[f:FOLLOWS]->(fwd:User {id: '$followed'})" +
                "RETURN f",
                parameters("follower", fwString, 
                            "followed", fdString)
            );

            if (result.hasNext()) 
                return true;
        }

        return false;
    }
    
    public boolean getFollowbyId(ObjectId followerId, ObjectId followedId) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String fwString = followerId.toHexString();
            String fdString = followedId.toHexString();
            Result result = session.run(
                "MATCH (fwr:User {id: '$follower'})-[f:FOLLOWS]->(fwd:User {id: '$followed'})" +
                "RETURN f",
                parameters("follower", fwString, 
                            "followed", fdString)
            );

            if (result.hasNext()) 
                return true;
        }

        return false;
    }

    public boolean checkFollow(Follow follow) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String fwString = follow.getFollowerId().toHexString();
            String fdString = follow.getFollowedId().toHexString();
            Result result = session.run(
                "MATCH (fwr:User {id: '$follower'})-[f:FOLLOWS]->(fwd:User {id: '$followed'})" +
                "RETURN f",
                parameters("follower", fwString, 
                            "followed", fdString)
            );

            if (result.hasNext()) 
                return true;
        }

        return false;
    }

    public boolean checkFollow(ObjectId followerId, ObjectId followedId) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String fwString = followerId.toHexString();
            String fdString = followedId.toHexString();
            Result result = session.run(
                "MATCH (fwr:User {id: '$follower'})-[f:FOLLOWS]->(fwd:User {id: '$followed'})" +
                "RETURN f",
                parameters("follower", fwString, 
                            "followed", fdString)
            );

            if (result.hasNext()) 
                return true;
        }

        return false;
    }

    // DELETE

    /**
     * Delete a follow from the graph database
     * @param follower
     * @param followed
     */
    public boolean deleteFollow(Reviewer follower, Reviewer followed) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String fwString = follower.getId().toHexString();
            String fdString = followed.getId().toHexString();
            session.run(
                "MATCH (fwr:User {id: '$follower'})" +
                "WITH fwr" +
                "MATCH (fwd:User {id: '$followed'})" +
                "WHERE (fwr)-[f:FOLLOWS]->(fwd)" +
                "DELETE f",
                parameters("user", fwString, 
                            "followed", fdString)
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a follow relationship between an user and another
     * @param follow
     */
    public boolean deleteFollow(Follow follow) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String fwString = follow.getFollowerId().toHexString();
            String fdString = follow.getFollowedId().toHexString();
            session.run(
                "MATCH (fwer:User {id: '$follower'})-[f:FOLLOWS]->(fwed:User {id: '$followed'}) " +
                "DELETE f", 
                parameters("follower", fwString, 
                            "followed", fdString)
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete a follow from the graph database
     * @param followerId
     * @param followedId
     */
    public boolean deleteFollow(ObjectId followerId, ObjectId followedId) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String fwString = followerId.toHexString();
            String fdString = followedId.toHexString();
            session.run(
                "MATCH (fwr:User {id: '$follower'}), (fwd:User {id: '$followed'})" +
                "WHERE (fwr)-[f:FOLLOWS]->(fwd)" +
                "DELETE f",
                parameters("user", fwString, 
                            "followed", fdString)
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
