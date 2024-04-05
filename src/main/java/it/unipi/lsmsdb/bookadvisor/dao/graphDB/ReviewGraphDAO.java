package it.unipi.lsmsdb.bookadvisor.dao.graphDB;
import static org.neo4j.driver.Values.parameters;

import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;

import it.unipi.lsmsdb.bookadvisor.model.book.Book;
import it.unipi.lsmsdb.bookadvisor.model.review.Review;
import it.unipi.lsmsdb.bookadvisor.model.user.RegisteredUser;

public class ReviewGraphDAO {
    private final Driver driver;

    public ReviewGraphDAO(String uri, String username, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public ReviewGraphDAO(Driver driver) {
        this.driver = driver;
    }

    public void close() {
        this.driver.close();
    }

    // CREATE 
    /**
     * Create a review relationship in the graph database
     * @param userId
     * @param bookId
     * @param rating
     */
    public void addReview(String userId, String bookId, int rating) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (usr:User {id: $user}), (bk:Book {id: $book}) " +
                "WHERE NOT (usr)-[:RATES]->(bk)" +
                "CREATE (usr)-[:RATES {stars: $rating}]->(bk)", 
                parameters("user", userId, 
                            "book", bookId, 
                            "rating", rating)
            );
        }
    }

    // READ
    /**
     * Get a review from the graph database
     * @param userId
     * @param bookId
     */
    public Review getReview(String userId, String bookId) {
        try (Session session = driver.session()) {
            Result result = session.run(
                "MATCH (usr:User {id: $user})-[r:RATES]->(bk:Book {id: $book})" +
                "RETURN r.stars AS rating",
                parameters("user", userId, 
                            "book", bookId)
            );

            if (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("rating").asNode();
                return new Review(node);
            }
            return null;
        }
    }

    // UPDATE
    /**
     * Update a review in the graph database
     * @param userId 
     * @param bookId
     * @param rating
     */
    public boolean updateReview(String userId, String bookId, int rating) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (usr:User {id: $user}), (bk:Book {id: $book})" +
                "WHERE (usr)-[r:RATES]->(bk)" +
                "SET r.rating = $rating",
                parameters("user", userId, 
                            "book", bookId, 
                            "rating", rating) 
            );
        } catch (Neo4jException e) {
            return false;
        }
        return true;
    }

    // DELETE

    /**
     * Delete a review from the graph database
     * @param userId
     * @param bookId
     */
    public void deleteReview(ObjectId userId, ObjectId bookId) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (usr:User {id: $user}), (bk:Book {id: $book})" +
                "WHERE (usr)-[r:RATES]->(bk)" +
                "DELETE r",
                parameters("user", userId, 
                            "book", bookId)
            );
        }
    }

    /**
     * Delete a review from the graph database
     * @param user
     * @param book
     */
    public void deleteReview(RegisteredUser user, Book book) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (usr:User {id: $user}), (bk:Book {id: $book})" +
                "WHERE (usr)-[r:RATES]->(bk)" +
                "DELETE r",
                parameters("user", user.getId(), 
                            "book", book.getId())
            );
        }
    }

}
