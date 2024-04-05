package it.unipi.lsmsdb.bookadvisor.dao.graphDB;

import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;

import it.unipi.lsmsdb.bookadvisor.model.follow.Follow;
import it.unipi.lsmsdb.bookadvisor.model.user.*;

import static org.neo4j.driver.Values.parameters;

import java.util.*;

import org.bson.types.ObjectId;

public class UserGraphDAO {
    private final Driver driver;

    public UserGraphDAO(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }
    
    public UserGraphDAO(Driver driver) {
        this.driver = driver;
    }

    public void close() {
        this.driver.close();
    }

    // CREATE OPERATIONS

    /**
     * Add an author to the graph database
     * @param author
     */
    public void addUser(Author author) {
        try (Session session = driver.session()) {
            // Create the node only if it hasn't been created
            session.run(
                "MERGE (u:User {id: $id}) " + 
                "ON CREATE SET u.fav_genres = $fav_genres, u.genres = $genres, u.spoken_lang = $spoken_lang", 
                parameters("id", author.getId(), 
                            "fav_genres", author.getFavouriteGenres(), 
                            "genres", author.getGenres(), 
                            "spoken_lang", author.getSpokenLanguages())
            );
        }
    }

    /**
     * Add a user to the graph database
     * @param user 
     */
    public void addUser(RegisteredUser user) {
        try (Session session = driver.session()) {
            // Create the node only if it hasn't been created
            session.run(
                "MERGE (u:User {id: $id}) " + 
                "ON CREATE SET u.fav_genres = $fav_genres, u.spoken_lang = $spoken_lang", 
                parameters("id", user.getId(), 
                            "fav_genres", user.getFavouriteGenres(), 
                            "spoken_lang", user.getSpokenLanguages())
            );
        }
    }

    // READ OPERATIONS

    public List<User> getAllUsers() {
        try (Session session = driver.session()) {
            Result result = session.run(
                "MATCH (u:User) " +
                "RETURN u"
            );

            List<User> userList = new ArrayList<>();
            while (result.hasNext()) {
                Node userNode = result.next().get("u").asNode();
                
                if(userNode.get("isAdmin").asBoolean())
                    userList.add(new Admin(userNode));
                else if(userNode.get("genres").asList(org.neo4j.driver.Value::asString).size() > 0)
                    userList.add(new Author(userNode));
                else if(userNode.get("fav_genres").asList(org.neo4j.driver.Value::asString).size() > 0)
                    userList.add(new RegisteredUser(userNode));
                else
                    userList.add(new User(userNode));
            }

            return userList;
        }
    }

    public User getUserById(ObjectId id) {
        try (Session session = driver.session()) {
            Result result = session.run(
                "MATCH (u:User {id: $id}) " +
                "RETURN u", 
                parameters("id", id)
            );

            if (result.hasNext()) {
                Node userNode = result.next().get("u").asNode();
                
                if(userNode.get("isAdmin").asBoolean())
                    return new Admin(userNode);
                else if(userNode.get("genres").asList(org.neo4j.driver.Value::asString).size() > 0)
                    return new Author(userNode);
                else if(userNode.get("fav_genres").asList(org.neo4j.driver.Value::asString).size() > 0)
                    return new RegisteredUser(userNode);
                else
                    return new User(userNode);
            }

            return null;
        }
    }

    // UPDATE OPERATIONS

    public boolean updateUser(ObjectId id, RegisteredUser user) {
        try(Session session = driver.session()) {
            session.run(
                "MATCH (u:User {id: $id}) " +
                "SET u.fav_genres = $fav_genres, u.spoken_lang = $spoken_lang",
                parameters("id", id, 
                            "fav_genres", user.getFavouriteGenres(), 
                            "spoken_lang", user.getSpokenLanguages())
            );
        } catch (Neo4jException e) {
            return false;
        }
        return true;
    }

    public boolean updateUser(ObjectId id, Author author) {
        try(Session session = driver.session()) {
            session.run(
                "MATCH (u:User {id: $id}) " +
                "SET u.fav_genres = $fav_genres, u.genres = $genres, u.spoken_lang = $spoken_lang",
                parameters("id", id, 
                            "fav_genres", author.getFavouriteGenres(), 
                            "genres", author.getGenres(), 
                            "spoken_lang", author.getSpokenLanguages())
            );
        } catch (Neo4jException e) {
            return false;
        }
        return true;
    }

    // DELETE OPERATIONS

    /**
     * Delete a follow relationship between an user and another
     * @param follow
     */
    public void deleteFollow(Follow follow) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (fwer:User {id: $follower})-[f:FOLLOWS]->(fwed:User {id: $followed}) " +
                "DELETE f", 
                parameters("follower", follow.getFollowerId(), 
                            "followed", follow.getFollowedId())
            );
        } 
    }

    /**
     * Delete a user from the graph database
     * @param user
     */
    public void deleteUser(User user) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (u:User {id: $id}) " +
                "DETACH DELETE u", 
                parameters("id", user.getId())
            );
        }
    }
    /**
     * Delete a user from the graph database using their id
     * @param userId
     */
    public void deleteUserById(ObjectId userId) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (u:User {id: $id}) " +
                "DETACH DELETE u", 
                parameters("id", userId)
            );
        }
    }

}
