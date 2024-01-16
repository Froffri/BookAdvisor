package it.unipi.lsmsdb.bookadvisor.dao.graphDB;

import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;

import it.unipi.lsmsdb.bookadvisor.model.user.*;

import static org.neo4j.driver.Values.parameters;

import java.util.*;

import org.neo4j.driver.AuthTokens;

public class UserGraphDAO {

    private Driver driver;

    public UserGraphDAO(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }
    
    public UserGraphDAO(Driver driver) {
        this.driver = driver;
    }

    public void close() {
        driver.close();
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
    
    /**
     * Add a follow relationship between two users
     * @param follower
     * @param followed
     */
    public void addFollow(RegisteredUser follower, RegisteredUser followed) {
        try (Session session = driver.session()) {
            // Check if the "follower" and "followed" nodes exist
            Result checkResult = session.run(
                "MATCH (fwer:User {id: $follower}) " +
                "MATCH (fwed:User {id: $followed}) " +
                "RETURN fwer, fwed", 
                parameters("follower", follower.getId(), "followed", followed.getId())
            );

            if (checkResult.hasNext()) {
                // Nodes exist; create the "follow" relationship
                session.run(
                    "MATCH (fwer:User {id: $follower}) " +
                    "MATCH (fwed:User {id: $followed}) " +
                    "CREATE (fwer)-[:FOLLOWS]->(fwed)", 
                    parameters("follower", follower.getId(), "followed", followed.getId())
                );
            } else {
                //@TODO
                // Handle the case where one or both nodes do not exist
                // You can log an error or handle the situation as needed
                System.out.println("One or both nodes do not exist.");
            }
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
                User user = new User(userNode);
                // Set other properties of the user object if needed
                userList.add(user);
            }

            return userList;
        }
    }


    // UPDATE OPERATIONS

    // DELETE OPERATIONS

    /**
     * Delete a follow relationship between an author and a user
     * @param follower
     * @param followed
     */
    public void deleteFollow(RegisteredUser follower, RegisteredUser followed) {
        try (Session session = driver.session()) {
            // Check if the "follower" and "followed" nodes exist
            Result checkResult = session.run(
                "MATCH (fwer:User {id: $follower}) " +
                "MATCH (fwed:User {id: $followed}) " +
                "RETURN fwer, fwed", 
                parameters("follower", follower.getId(), "followed", followed.getId())
            );

            if (checkResult.hasNext()) {
                // Nodes exist; delete the "follow" relationship
                session.run(
                    "MATCH (fwer:User {id: $follower})-[r:FOLLOWS]->(fwed:User {id: $followed}) " +
                    "DELETE r", 
                    parameters("follower", follower.getId(), "followed", followed.getId())
                );
            } else {
                //@TODO
                // Handle the case where one or both nodes do not exist
                // You can log an error or handle the situation as needed
                System.out.println("One or both nodes do not exist.");
            }
        } 
    }



}
