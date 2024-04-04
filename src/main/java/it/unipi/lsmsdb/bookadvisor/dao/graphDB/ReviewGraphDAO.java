package it.unipi.lsmsdb.bookadvisor.dao.graphDB;
import static org.neo4j.driver.Values.parameters;

import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;

public class ReviewGraphDAO {
    private final Driver driver;

    public ReviewGraphDAO(String uri, String username, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public ReviewGraphDAO(Driver driver) {
        this.driver = driver;
    }

    public void close() {
        driver.close();
    }

    // CREATE OPERATIONS

    /**
     * Create a review relationship in the graph database
     * @param userId
     * @param bookId
     * @param rating
     */
    public void createReview(String userId, String bookId, int rating) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (usr:User {id: $user}), (bk:Book {id: $book}) " +
                "WHERE NOT (usr)-[:RATES]->(bk)" +
                "CREATE (usr)-[:RATES {stars: $rating}]->(bk)", 
                parameters("user", userId, "book", bookId, "rating", rating)
            );
        }
    }

// @TODO: RIGUARDA TUTTOOOOOOO

    public void updateReview(String reviewId, String content, int rating) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (r:Review {id: $reviewId}) SET r.content = $content, r.rating = $rating",
                        Values.parameters("reviewId", reviewId, "content", content, "rating", rating));
                return null;
            });
        }
    }

    public void deleteReview(String reviewId) {
        try (Session session = driver.session()) {
            session.writeTransaction(tx -> {
                tx.run("MATCH (r:Review {id: $reviewId}) DELETE r",
                        Values.parameters("reviewId", reviewId));
                return null;
            });
        }
    }

    public void getReview(String reviewId) {
        try (Session session = driver.session()) {
            session.readTransaction(tx -> {
                Result result = tx.run("MATCH (r:Review {id: $reviewId}) RETURN r",
                        Values.parameters("reviewId", reviewId));
                while (result.hasNext()) {
                    Record record = result.next();
                    Node reviewNode = record.get("r").asNode();
                    // Process the review node
                    System.out.println(reviewNode);
                }
                return null;
            });
        }
    }

}
