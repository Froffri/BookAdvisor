package it.unipi.lsmsdb.bookadvisor.dao.graphDB;

import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;

import it.unipi.lsmsdb.bookadvisor.model.user.*;

import static org.neo4j.driver.Values.parameters;

import java.util.*;

import org.bson.types.ObjectId;

public class UserGraphDAO {
    private final Neo4jConnector connector;

    public UserGraphDAO(Neo4jConnector connector) {
        this.connector = connector;
    }

    // CREATE OPERATIONS

    // /**
    //  * Add an author to the graph database
    //  * @param author
    //  */
    // public boolean addUser(Author author) {
    //     try (Session session = connector.getSession()) {
    //         // Create the node only if it hasn't been created
    //         session.run(
    //             "MERGE (u:User {id: $id}) " + 
    //             "ON CREATE SET u.fav_genres = $fav_genres", 
    //             parameters("id", author.getId(), 
    //                         "fav_genres", author.getFavouriteGenresString())
    //         );
    //     } catch (Exception e) {
    //         System.err.println("Error while inserting author: " + e.getMessage());
    //         return false;
    //     }
    //     return true;
    // }

    /**
     * Add a user to the graph database
     * @param user 
     */
    public boolean addUser(ObjectId id, Reviewer user) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String idString = id.toHexString();
    
            // Create the node only if it hasn't been created
            return session.run(
                "MERGE (u:User {id: $id}) " + 
                "ON CREATE SET u.fav_genres = $fav_genres, u.nickname = $nickname", 
                parameters("id", idString, 
                            "fav_genres", user.getFavouriteGenresString(),
                            "nickname", user.getNickname())
            ).consume().counters().nodesCreated() > 0;
        } catch (Exception e) {
            System.err.println("Error while inserting user: " + e.getMessage());
            return false;
        }
    }

    // /**
    //  * Add a user to the graph database
    //  * @param user 
    //  */
    // public boolean addUser(User user) {
    //     try (Session session = connector.getSession()) {
    //         // Create the node only if it hasn't been created
    //         session.run(
    //             "MERGE (u:User {id: $id}) "/* + 
    //             "ON CREATE SET u.fav_genres = $fav_genres, u.spoken_lang = $spoken_lang", 
    //             parameters("id", user.getId())*/
    //         );
    //     } catch (Exception e) {
    //         System.err.println("Error while inserting user: " + e.getMessage());
    //         return false;
    //     }
    //     return true;
    // }

    // READ OPERATIONS

    public List<Reviewer> getAllUsers() {
        try (Session session = connector.getSession()) {
            Result result = session.run(
                "MATCH (u:User) " +
                "RETURN u"
            );

            List<Reviewer> userList = new ArrayList<>();
            while (result.hasNext()) {
                Node userNode = result.next().get("u").asNode();
                
                // if(userNode.get("isAdmin").asBoolean())
                //     userList.add(new Admin(userNode));
                // else if(userNode.get("genres").asList(org.neo4j.driver.Value::asString).size() > 0)
                //     userList.add(new Author(userNode));
                // else if(userNode.get("fav_genres").asList(org.neo4j.driver.Value::asString).size() > 0)
                    userList.add(new Reviewer(userNode));
                // else
                //     userList.add(new User(userNode));
            }

            return userList;
        }
    }

    public Reviewer getUserById(ObjectId id) {
        try (Session session = connector.getSession()) {
            Result result = session.run(
                "MATCH (u:User {id: $id}) " +
                "RETURN u", 
                parameters("id", id.toHexString())
            );

            if (result.hasNext()) {
                Node userNode = result.next().get("u").asNode();
                
                // if(userNode.get("isAdmin").asBoolean())
                //     return new Admin(userNode);
                // else if(userNode.get("genres").asList(org.neo4j.driver.Value::asString).size() > 0)
                //     return new Author(userNode);
                // else if(userNode.get("fav_genres").asList(org.neo4j.driver.Value::asString).size() > 0)
                    return new Reviewer(userNode);
                // else
                //     return new User(userNode);
            }

            return null;
        }
    }

    // UPDATE OPERATIONS

    public boolean updateUser(Reviewer user) {
        try(Session session = connector.getSession()) {
            return session.run(
                "MATCH (u:User {id: $id}) " +
                "SET u.fav_genres = $fav_genres",
                parameters("id", user.getId().toHexString(), 
                            "fav_genres", user.getFavouriteGenresString())
            ).consume().counters().containsUpdates();
        } catch (Neo4jException e) {
            return false;
        }
    }

    // public boolean updateUser(Author author) {
    //     try(Session session = connector.getSession()) {
    //         session.run(
    //             "MATCH (u:User {id: $id}) " +
    //             "SET u.fav_genres = $fav_genres, u.genres = $genres, u.spoken_lang = $spoken_lang",
    //             parameters("id", author.getId(), 
    //                         "fav_genres", author.getFavouriteGenresString(), 
    //                         "genres", author.getGenresString(), 
    //                         "spoken_lang", author.getSpokenLanguagesString())
    //         );
    //     } catch (Neo4jException e) {
    //         return false;
    //     }
    //     return true;
    // }

    // DELETE OPERATIONS

    /**
     * Delete a user from the graph database
     * @param user
     */
    public boolean deleteUser(Reviewer user) {
        try (Session session = connector.getSession()) {
            return session.run(
                "MATCH (u:User {id: $id}) " +
                "DETACH DELETE u", 
                parameters("id", user.getId().toHexString())
            ).consume().counters().nodesDeleted() > 0;
        } catch (Neo4jException e) {
            System.err.println("Error while deleting user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a user from the graph database using their id
     * @param userId
     */
    public boolean deleteUserById(ObjectId userId) {
        try (Session session = connector.getSession()) {
            return session.run(
                "MATCH (u:User {id: $id}) " +
                "DETACH DELETE u", 
                parameters("id", userId.toHexString())
            ).consume().counters().nodesDeleted() > 0;
        } catch (Neo4jException e) {
            System.err.println("Error while deleting user: " + e.getMessage());
            return false;
        }
    }

}
