package it.unipi.lsmsdb.bookadvisor.dao.graphDB;

import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;

import it.unipi.lsmsdb.bookadvisor.model.review.Review;

import static org.neo4j.driver.Values.parameters;

public class ReviewGraphDAO {
    private final Neo4jConnector connector;

    public ReviewGraphDAO(Neo4jConnector connector) {
        this.connector = connector;
    }

    // CREATE

    /**
     * Create a review relationship in the graph database
     *
     * @param userId
     * @param bookId
     * @param rating
     */
    public boolean addReview(ObjectId userId, ObjectId bookId, int rating) {
        try (Session session = connector.getSession()) {
            return session.run(
                    "MERGE (usr:User {id: $user})-[r:RATES]->(bk:Book {id: $book})" +
                    "ON CREATE SET r.stars = $rating",
                    parameters("user", userId.toHexString(),
                            "book", bookId.toHexString(),
                            "rating", rating)
            ).consume().counters().relationshipsCreated() > 0;
        } catch (Neo4jException e) {
            return false;
        }
    }

    /**
     * Create a review relationship in the graph database
     *
     * @param review
     */
    public boolean addReview(Review review) {
        try (Session session = connector.getSession()) {
            return session.run(
                    "MERGE (usr:User {id: $user})-[r:RATES]->(bk:Book {id: $book}) " +
                    "ON CREATE SET r.stars = $rating",
                    parameters("user", review.getUserId().toHexString(),
                            "book", review.getBookId().toHexString(),
                            "rating", review.getStars())
            ).consume().counters().relationshipsCreated() > 0;
        } catch (Neo4jException e) {
            return false;
        }
    }

    // READ

    /**
     * Get a review from the graph database
     *
     * @param userId
     * @param bookId
     */
    public Review getReview(ObjectId userId, ObjectId bookId) {
        try (Session session = connector.getSession()) {
            Result result = session.run(
                    "MATCH (usr:User {id: $user})-[r:RATES]->(bk:Book {id: $book}) " +
                    "RETURN r.stars AS rating",
                    parameters("user", userId.toHexString(),
                            "book", bookId.toHexString())
            );

            if (result.hasNext()) {
                Record record = result.next();
                Node node = record.get("rating").asNode();
                return new Review(node);
            }
            return null;
        }
    }

    public boolean checkReview(ObjectId userId, ObjectId bookId) {
        try (Session session = connector.getSession()) {
            return session.run(
                    "MATCH (usr:User {id: $user})-[r:RATES]->(bk:Book {id: $book}) " +
                    "RETURN r.stars AS rating",
                    parameters("user", userId.toHexString(),
                            "book", bookId.toHexString())
            ).hasNext();
        }
    }

    // UPDATE

    /**
     * Update a review in the graph database
     *
     * @param userId
     * @param bookId
     * @param rating
     */
    public boolean updateReview(ObjectId userId, ObjectId bookId, int rating) {
        try (Session session = connector.getSession()) {
            return session.run(
                    "MATCH (usr:User {id: $user})-[r:RATES]->(bk:Book {id: $book}) " +
                    "SET r.stars = $rating",
                    parameters("user", userId.toHexString(),
                            "book", bookId.toHexString(),
                            "rating", rating)
            ).consume().counters().containsUpdates();
        } catch (Neo4jException e) {
            return false;
        }
    }

    /**
     * Update a review in the graph database
     *
     * @param review
     */
    public boolean updateReview(Review review) {
        try (Session session = connector.getSession()) {
            return session.run(
                    "MATCH (usr:User {id: $user})-[r:RATES]->(bk:Book {id: $book}) " +
                    "SET r.stars = $rating",
                    parameters("user", review.getUserId().toHexString(),
                            "book", review.getBookId().toHexString(),
                            "rating", review.getStars())
            ).consume().counters().containsUpdates();
        } catch (Neo4jException e) {
            return false;
        }
    }

    // DELETE

    /**
     * Delete a review from the graph database
     *
     * @param userId
     * @param bookId
     */
    public boolean deleteReview(ObjectId userId, ObjectId bookId) {
        try (Session session = connector.getSession()) {
            return session.run(
                    "MATCH (usr:User {id: $user})-[r:RATES]->(bk:Book {id: $book}) " +
                    "DELETE r",
                    parameters("user", userId.toHexString(),
                            "book", bookId.toHexString())
            ).consume().counters().relationshipsDeleted() > 0;
        } catch (Neo4jException e) {
            return false;
        }
    }

    /**
     * Delete a review from the graph database
     *
     * @param review
     */
    public boolean deleteReview(Review review) {
        try (Session session = connector.getSession()) {
            return session.run(
                    "MATCH (usr:User {id: $user})-[r:RATES]->(bk:Book {id: $book}) " +
                    "DELETE r",
                    parameters("user", review.getUserId().toHexString(),
                            "book", review.getBookId().toHexString())
            ).consume().counters().relationshipsDeleted() > 0;
        } catch (Neo4jException e) {
            return false;
        }
    }
}
