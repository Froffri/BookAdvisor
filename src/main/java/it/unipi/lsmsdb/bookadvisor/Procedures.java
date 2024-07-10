package it.unipi.lsmsdb.bookadvisor;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.*;

import static org.neo4j.driver.Values.parameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Projections.computed;
import static com.mongodb.client.model.Projections.fields;

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.MongoDBConnector;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.Neo4jConnector;
import it.unipi.lsmsdb.bookadvisor.model.book.Book;


public class Procedures {
    private final Neo4jConnector graphConnector;
    private final MongoDBConnector connector;

    
    public Procedures(Neo4jConnector graphConnector, MongoDBConnector connector) {
        this.graphConnector = graphConnector;
        this.connector = connector;
    }

    // Given an author, find the average rating of their books, the number of ratings, and the average rating per country and number of ratings per country
    public List<Document> calculateAuthorStats(ObjectId authorId) {
        // Get the database and collection
        MongoDatabase database = connector.getDatabase();
        MongoCollection<Document> collection = database.getCollection("books");
    
        // Define the aggregation pipeline stages
        AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(
                // Match stage
                new Document("$match", new Document("authors.id", authorId)),
    
                // AddFields stage for averageRating
                new Document("$addFields", new Document("averageRating",
                        new Document("$cond", Arrays.asList(
                                new Document("$eq", Arrays.asList("$numRatings", 0)),
                                0,
                                new Document("$divide", Arrays.asList("$sumStars", "$numRatings"))
                        ))
                )),
    
                // AddFields stage for countryRatings
                new Document("$addFields", new Document("countryRatings",
                        new Document("$map", new Document("input",
                                new Document("$objectToArray", "$ratings_agg_by_nat"))
                                .append("as", "rating")
                                .append("in", new Document("country", "$$rating.k")
                                        .append("data", new Document("averageRating",
                                                new Document("$cond", Arrays.asList(
                                                        new Document("$eq", Arrays.asList("$$rating.v.cardinality", 0)),
                                                        0,
                                                        new Document("$divide", Arrays.asList("$$rating.v.sum_stars", "$$rating.v.cardinality"))
                                                )))
                                                .append("numRatings", "$$rating.v.cardinality")))))),
    
                // AddFields stage for detailedCountryRatings
                new Document("$addFields", new Document("detailedCountryRatings",
                        new Document("$reduce", new Document("input", "$countryRatings")
                                .append("initialValue", Arrays.asList())
                                .append("in", new Document("$concatArrays", Arrays.asList("$$value", Arrays.asList(
                                        new Document("country", "$$this.country")
                                                .append("averageRating", "$$this.data.averageRating")
                                                .append("numRatings", "$$this.data.numRatings")))))))),
    
                // Project stage
                new Document("$project", new Document("bookTitle", "$title")
                        .append("bookRating", "$averageRating")
                        .append("bookTotalRatings", "$numRatings")
                        .append("bookCountryDetails", new Document("$map",
                                new Document("input", "$detailedCountryRatings")
                                        .append("as", "countryDetail")
                                        .append("in", new Document("country", "$$countryDetail.country")
                                                .append("averageRating", new Document("$round", Arrays.asList("$$countryDetail.averageRating", 2)))
                                                .append("numRatings", "$$countryDetail.numRatings"))))),
    
                // Sorting stage
                new Document("$sort", new Document("bookTitle", 1))
        ));
    
        // Collect the results
        List<Document> results = new ArrayList<>();
        for (Document doc : iterable) {
            results.add(doc);
        }
    
        return results;
    }

    

    // Find the most famous books for a given genre in each country
    public List<Document> findMostFamousBooks(String genre) {
        MongoDatabase database = connector.getDatabase();
        MongoCollection<Document> collection = database.getCollection("books");
    
        AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(
            new Document("$match", new Document("genre", genre)),
            new Document("$project", new Document("title", 1)
                    .append("ratings_agg_by_nat", 1)
                    .append("image_url", 1)
                    .append("countryReviews", new Document("$map", new Document("input",
                            new Document("$objectToArray", "$ratings_agg_by_nat"))
                            .append("as", "country")
                            .append("in", new Document("country", "$$country.k")
                                    .append("numReviews", "$$country.v.cardinality")
                                    .append("bookTitle", "$title")
                                    .append("bookId", "$_id")
                                    .append("imageUrl", "$image_url"))))),
            new Document("$unwind", "$countryReviews"),
            new Document("$group", new Document("_id", "$countryReviews.country")
                    .append("mostFamousBook", new Document("$first", new Document("title", "$countryReviews.bookTitle")
                            .append("bookId", "$countryReviews.bookId")
                            .append("numReviews", "$countryReviews.numReviews")
                            .append("imageUrl", "$countryReviews.imageUrl")))
                    .append("maxNumReviews", new Document("$max", "$countryReviews.numReviews"))),
            new Document("$project", new Document("_id", 0)
                    .append("country", "$_id")
                    .append("title", "$mostFamousBook.title")
                    .append("bookId", "$mostFamousBook.bookId")
                    .append("numReviews", "$mostFamousBook.numReviews")
                    .append("imageUrl", "$mostFamousBook.imageUrl"))
        ));
    
        List<Document> results = new ArrayList<>();
        for (Document doc : iterable) {
            results.add(doc);
        }
    
        return results;
    }
    
    // Find the top useful reviews for a given user
    public List<Document> findMostUsefulReviews(String username) {
        MongoDatabase database = connector.getDatabase();
        MongoCollection<Document> collection = database.getCollection("books");
    
        AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(
            new Document("$match", new Document("most_10_useful_reviews.nickname", username)),
            new Document("$addFields", new Document("most_10_useful_reviews",
                    new Document("$map", new Document("input", "$most_10_useful_reviews")
                            .append("as", "review")
                            .append("in", new Document("$mergeObjects", Arrays.asList(
                                    "$$review",
                                    new Document("usefulness",
                                            new Document("$subtract", Arrays.asList("$$review.count_up_votes", "$$review.count_down_votes")))
                            )))))),
            new Document("$addFields", new Document("most_10_useful_reviews",
                    new Document("$filter", new Document("input", "$most_10_useful_reviews")
                            .append("as", "review")
                            .append("cond", new Document("$eq", Arrays.asList("$$review.nickname", username)))))),
            new Document("$addFields", new Document("most_10_useful_reviews",
                    new Document("$slice", Arrays.asList(
                            new Document("$sortArray",
                                    new Document("input", "$most_10_useful_reviews")
                                            .append("sortBy", new Document("usefulness", -1))),
                            3)))),
            new Document("$project", new Document("_id", 0)
                    .append("title", "$title")
                    .append("author", "$authors")
                    .append("language", "$language")
                    .append("year", "$year")
                    .append("genre", "$genre")
                    .append("image_url", "$image_url")
                    .append("num_pages", "$num_pages")
                    .append("most_useful_reviews", "$most_10_useful_reviews"))
        ));
    
        List<Document> results = new ArrayList<>();
        for (Document doc : iterable) {
            results.add(doc);
        }
    
        return results;
    }

//     public List<Map<String, Object>> getBookRecommendations(Object userId) {
//         try (Session session = graphConnector.getSession()) {
//             String cypherQuery =
//                     "MATCH (user:User {id: '$userId'}) " +
//                     "MATCH (book1:Book)<-[r1:RATES]-(user)-[f:FOLLOWS]->(other:User)-[r2:RATES]->(recommended:Book) " +
//                     "WHERE r1.rating > 3 AND book1 <> recommended AND r2.rating > 3 " +
//                     "WITH book1, recommended, r2.rating AS recommendedRating, f " +
//                     "ORDER BY f ASC, recommendedRating DESC " +
//                     "WITH book1, COLLECT({recommended: recommended, score: recommendedRating})[..3] AS recommendations " +
//                     "RETURN book1, recommendations";

//             Result result = session.run(cypherQuery, parameters("userId", userId));
//             List<Map<String, Object>> results = new ArrayList<>();

//             while (result.hasNext()) {
//                 Record record = result.next();
//                 Map<String, Object> bookRecommendations = new HashMap<>();
//                 bookRecommendations.put("book1", record.get("book1").asMap());
//                 List<Object> recommendations = record.get("recommendations").asList(Value::asMap);
//                 bookRecommendations.put("recommendations", recommendations);
//                 results.add(bookRecommendations);
//             }

//             return results;
//         }
//     }

    /**
     * This function is used to get book recommendations for a user.
     * It works by first finding books that the user has rated more than 3.
     * Then it finds other users who the current user follows and have also rated their books more than 3.
     * It ensures that the recommended books are not the same as the ones the user has already rated.
     * Additionally, it now also ensures that the recommended books are in the same language as the books the user has rated.
     * The recommendations are then ordered by the follow date and the rating of the recommended books.
     * The top 3 recommendations are collected for each book the user has rated.
     * Finally, it returns a list of maps, where each map contains a book the user has rated and the corresponding book recommendations.
     *
     * @param userId The ID of the user for whom the book recommendations are to be found.
     * @return A list of maps containing the books the user has rated and the corresponding book recommendations.
     */
    public List<Map<String, Object>> getBookRecommendations(ObjectId userId) {
        try (Session session = graphConnector.getSession()) {
                String cypherQuery =
                        "MATCH (user:User {id: '$userId'}) " +
                        "MATCH (book1:Book)<-[r1:RATES]-(user)-[f:FOLLOWS]->(other:User)-[r2:RATES]->(recommended:Book) " +
                        "WHERE r1.rating > 3 AND book1 <> recommended AND r2.rating > 3 AND book1.language = recommended.language " +
                        "WITH book1, recommended, r2.rating AS recommendedRating, f " +
                        "ORDER BY f ASC, recommendedRating DESC " +
                        "WITH book1, COLLECT({recommended: recommended, score: recommendedRating})[..3] AS recommendations " +
                        "RETURN book1, recommendations";

                Result result = session.run(cypherQuery, parameters("userId", userId.toHexString()));
                List<Map<String, Object>> results = new ArrayList<>();

                while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> bookRecommendations = new HashMap<>();
                bookRecommendations.put("book1", record.get("book1").asMap());
                List<Object> recommendations = record.get("recommendations").asList(Value::asMap);
                bookRecommendations.put("recommendations", recommendations);
                results.add(bookRecommendations);
                }

                return results;
        }
    }

    /** 
     * Find books that both the given user (u1) and other users (u2) have rated. 
     * Only consider pairs where the difference in their ratings is less than 2.
     * Collect the titles of these commonly rated books.
     * Find any followers of the other users (u2) who have rated these common books. 
     * Collect the ratings given by these followers to the common books.
     * Filter out any followers who have rated fewer than 2 of the common books.
     * Check if the given user (u1) and the other users (u2) have any favourite genres in common. 
     * If they do, go to the next step. If not, the other user is not considered to have similar tastes.
     * 
     * @param userId The ID of the user for whom the similar tastes are to be found.
     * @return A list of maps containing the ID and nickname of the other users who have similar tastes, 
     *          along with the titles of the common books and the count of the followers who have rated these books.
     *          The results are ordered by the follower count in descending order, and only the top 10 results are returned.
     */
    public List<Map<String, Object>> getUsersWithSimilarTastes(ObjectId userId) {
        try (Session session = graphConnector.getSession()) {
            String cypherQuery =
                    "MATCH (u1:User {id: '$userId'})-[r1:RATES]->(book:Book)<-[r2:RATES]-(u2:User) " +
                    "WHERE id(u1) < id(u2) " +
                    "WITH u1, u2, book, r1, r2 " +
                    "WHERE abs(r1.rating - r2.rating) < 2 " +
                    "WITH u1, u2, COLLECT(DISTINCT book.title) AS commonBooks " +
                    "MATCH (u2)<-[:FOLLOWS]-(follower) " +
                    "MATCH (follower)-[r3:RATES]->(commonBook:Book) " +
                    "WHERE commonBook.title IN commonBooks " +
                    "WITH u1, u2, commonBooks, follower, COLLECT(r3.rating) AS followerRatings " +
                    "WITH u1, u2, commonBooks, follower, size(followerRatings) AS followerRatingCount " +
                    "WHERE followerRatingCount > 1 " +
                    "WITH u1, u2, commonBooks, followerRatingCount " +
                    "WHERE SIZE([genre IN u1.favouriteGenres WHERE genre IN u2.favouriteGenres]) > 0 " +
                    "RETURN u2.id AS user2, u2.nickname AS Nickname, commonBooks, followerRatingCount " +
                    "ORDER BY followerRatingCount DESC " +
                    "LIMIT 10";
    
            Result result = session.run(cypherQuery, parameters("userId", userId.toHexString()));
            List<Map<String, Object>> results = new ArrayList<>();
    
            while (result.hasNext()) {
                Record record = result.next();
                Map<String, Object> userWithSimilarTastes = new HashMap<>();
                userWithSimilarTastes.put("user2", record.get("user2").asString());
                userWithSimilarTastes.put("Nickname", record.get("Nickname").asString());
                userWithSimilarTastes.put("commonBooks", record.get("commonBooks").asList(Value::asString));
                userWithSimilarTastes.put("followerRatingCount", record.get("followerRatingCount").asInt());
                results.add(userWithSimilarTastes);
            }
    
            return results;
        }
    }

    public static void main(String[] args) {
        String username = "AliceJones89"; // Replace with the desired username
        // new Procedures().findMostUsefulReviews(username);
    }

    
}
