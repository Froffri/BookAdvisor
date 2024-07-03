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
    public void addFollow(Follow follow) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (fwer:User {id: $follower})" +
                "WITH fwer" +
                "MATCH (fwed:User {id: $followed})" +
                "WHERE NOT (fwer)-[:FOLLOWS]->(fwed)" +
                "CREATE (fwer)-[:FOLLOWS]->(fwed)", 
                parameters("follower", follow.getFollowerId(), 
                            "followed", follow.getFollowedId())
            );
        } 
    }

    /**
     * Add a follow relationship between two users
     * @param follower
     * @param followed
     */
    public void addFollow(Reviewer follower, Reviewer followed) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (fwer:User {id: $follower})" +
                "WITH fwer" +
                "MATCH (fwed:User {id: $followed}) " +
                "WHERE NOT (fwer)-[:FOLLOWS]->(fwed)" +
                "CREATE (fwer)-[:FOLLOWS]->(fwed)", 
                parameters("follower", follower.getId(), 
                            "followed", followed.getId())
            );
        } 
    }

    /**
     * Add a follow relationship between two users using their ids
     * @param followerId
     * @param followedId
     */
    public void addFollowByIds(ObjectId followerId, ObjectId followedId) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (fwer:User {id: $follower})" +
                "WITH fwer" +
                "MATCH (fwed:User {id: $followed}) " +
                "WHERE NOT (fwer)-[:FOLLOWS]->(fwed)" +
                "CREATE (fwer)-[:FOLLOWS]->(fwed)", 
                parameters("follower", followerId, 
                            "followed", followedId)
            );
        } 
    }

    // READ 

    public boolean getFollow(Reviewer follower, Reviewer followed) {
        try (Session session = connector.getSession()) {
            Result result = session.run(
                "MATCH (fwr:User {id: $follower})-[f:FOLLOWS]->(fwd:User {id: $followed})" +
                "RETURN f",
                parameters("follower", follower.getId(), 
                            "followed", followed.getId())
            );

            if (result.hasNext()) 
                return true;
        }

        return false;
    }
    
    public boolean getFollowbyId(ObjectId followerId, ObjectId followedId) {
        try (Session session = connector.getSession()) {
            Result result = session.run(
                "MATCH (fwr:User {id: $follower})-[f:FOLLOWS]->(fwd:User {id: $followed})" +
                "RETURN f",
                parameters("follower", followerId, 
                            "followed", followedId)
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
    public void deleteFollow(Reviewer follower, Reviewer followed) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (fwr:User {id: $follower})" +
                "WITH fwr" +
                "MATCH (fwd:User {id: $followed})" +
                "WHERE (fwr)-[f:FOLLOWS]->(fwd)" +
                "DELETE f",
                parameters("user", follower.getId(), 
                            "followed", followed.getId())
            );
        }
    }

    /**
     * Delete a follow relationship between an user and another
     * @param follow
     */
    public void deleteFollow(Follow follow) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (fwer:User {id: $follower})-[f:FOLLOWS]->(fwed:User {id: $followed}) " +
                "DELETE f", 
                parameters("follower", follow.getFollowerId(), 
                            "followed", follow.getFollowedId())
            );
        } 
    }

    /**
     * Delete a follow from the graph database
     * @param followerId
     * @param followedId
     */
    public void deleteFollow(ObjectId followerId, ObjectId followedId) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (fwr:User {id: $follower}), (fwd:User {id: $followed})" +
                "WHERE (fwr)-[f:FOLLOWS]->(fwd)" +
                "DELETE f",
                parameters("user", followerId, 
                            "followed", followedId)
            );
        }
    }

}
