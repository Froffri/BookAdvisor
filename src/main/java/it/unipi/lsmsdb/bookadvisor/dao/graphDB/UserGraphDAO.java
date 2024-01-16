package it.unipi.lsmsdb.bookadvisor.dao.graphDB;

import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.exceptions.Neo4jException;
import static org.neo4j.driver.Values.parameters;

import org.neo4j.driver.AuthTokens;

public class UserGraphDAO {

    private Driver driver;

    public UserGraphDAO(String uri, String user, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }
    
    public UserGraphDAO(Driver driver) {
        this.driver = driver;
    }

    public void addUser(Integer id, String favourite_genre[], String genre[], String spoken_languages[]){
        try (Session session = driver.session()) {
            // Create the node only if it hasn't been created
            session.run(
                "MERGE (u:User {id: $id}) " + 
                "ON CREATE SET u.fav_genre = $fav_genre, u.genre = $genre, u.spoken_lang = $spoken_lang", 
                parameters("id", id, 
                            "fav_genre", favourite_genre, 
                            "genre", genre, 
                            "spoken_lang", spoken_languages)
            );
        }
    }
    
    public void addFollow(Integer follower, Integer followed) {
        try (Session session = driver.session()) {
            // Check if the "follower" and "followed" nodes exist
            Result checkResult = session.run(
                "MATCH (fwer:User {id: $follower}) " +
                "MATCH (fwed:User {id: $followed}) " +
                "RETURN fwer, fwed", 
                parameters("follower", follower, "followed", followed)
            );

            if (checkResult.hasNext()) {
                // Nodes exist; create the "follow" relationship
                session.run(
                    "MATCH (fwer:User {id: $follower}) " +
                    "MATCH (fwed:User {id: $followed}) " +
                    "CREATE (fwer)-[:FOLLOWS]->(fwed)", 
                    parameters("follower", follower, "followed", followed)
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
