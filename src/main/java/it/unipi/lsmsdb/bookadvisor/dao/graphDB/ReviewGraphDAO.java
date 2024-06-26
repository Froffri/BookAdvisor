package it.unipi.lsmsdb.bookadvisor.dao.graphDB;
import static org.neo4j.driver.Values.parameters;

import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;

import it.unipi.lsmsdb.bookadvisor.model.book.Book;
import it.unipi.lsmsdb.bookadvisor.model.review.Review;
import it.unipi.lsmsdb.bookadvisor.model.user.Reviewer;

public class ReviewGraphDAO {
    private final Neo4jConnector connector;

    public ReviewGraphDAO(Neo4jConnector connector) {
        this.connector = connector;
    }

    // CREATE 
    /**
     * Create a review relationship in the graph database
     * @param userId
     * @param bookId
     * @param rating
     */
    public boolean addReview(String userId, String bookId, int rating) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (usr:User {id: $user})" +
                "WITH usr" +
                "MATCH (bk:Book {id: $book})" +
                "WHERE NOT (usr)-[:RATES]->(bk)" +
                "CREATE (usr)-[:RATES {stars: $rating}]->(bk)", 
                parameters("user", userId, 
                            "book", bookId, 
                            "rating", rating)
            );
        } catch (Neo4jException e) {
            return false;
        }
        return true;
    }
    /**
     * Create a review relationship in the graph database
     * @param review
     */
    public boolean addReview(Review review) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (usr:User {id: $user})" +
                "WITH usr" +
                "MATCH (bk:Book {id: $book})" +
                "WHERE NOT (usr)-[:RATES]->(bk)" +
                "CREATE (usr)-[:RATES {stars: $rating}]->(bk)", 
                parameters("user", review.getUserId(), 
                            "book", review.getBookId(), 
                            "rating", review.getStars())
            );
        } catch (Neo4jException e) {
            return false;
        }
        return true;
    }

    // READ
    /**
     * Get a review from the graph database
     * @param userId
     * @param bookId
     */
    public Review getReview(String userId, String bookId) {
        try (Session session = connector.getSession()) {
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
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (usr:User {id: $user})" +
                "WITH usr" +
                "MATCH (bk:Book {id: $book})" +
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

    /**
     * Update a review in the graph database
     * @param review
     */
    public boolean updateReview(Review review) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (usr:User {id: $user})" +
                "WITH usr" +
                "MATCH (bk:Book {id: $book})" +
                "WHERE (usr)-[r:RATES]->(bk)" +
                "SET r.rating = $rating",
                parameters("user", review.getUserId(), 
                            "book", review.getBookId(), 
                            "rating", review.getStars()) 
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
    public boolean deleteReview(ObjectId userId, ObjectId bookId) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (usr:User {id: $user})" +
                "WITH usr" +
                "MATCH (bk:Book {id: $book})" +
                "WHERE (usr)-[r:RATES]->(bk)" +
                "DELETE r",
                parameters("user", userId, 
                            "book", bookId)
            );
        } catch (Neo4jException e) {
            return false;
        }
        return true;
    }

    /**
     * Delete a review from the graph database
     * @param userId
     * @param bookId
     */
    public boolean deleteReview(Review review) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (usr:User {id: $user})" +
                "WITH usr" +
                "MATCH (bk:Book {id: $book})" +
                "WHERE (usr)-[r:RATES]->(bk)" +
                "DELETE r",
                parameters("user", review.getUserId(), 
                            "book", review.getBookId())
            );
        } catch (Neo4jException e) {
            return false;
        }
        return true;
    }

    /**
     * Delete a review from the graph database
     * @param user
     * @param book
     */
    public boolean deleteReview(Reviewer user, Book book) {
        try (Session session = connector.getSession()) {
            session.run(
                "MATCH (usr:User {id: $user})" +
                "WITH usr" +
                "MATCH (bk:Book {id: $book})" +
                "WHERE (usr)-[r:RATES]->(bk)" +
                "DELETE r",
                parameters("user", user.getId(), 
                            "book", book.getId())
            );
        } catch (Neo4jException e) {
            return false;
        }
        return true;
    }

}
