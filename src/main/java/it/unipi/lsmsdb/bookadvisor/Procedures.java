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

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.MongoDBConnector;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.Neo4jConnector;

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

    /**
     * Retrieves a list of books rated by users followed by a given user,
     * filtered by the languages spoken by the user, and orders the results 
     * in descending order of rating, returning up to 5 results.
     *
     * @param userId The ObjectId of the user for whom the recommendations are generated.
     * @param languages A list of languages spoken by the user, used to filter the books.
     * @return A list of maps where each map represents a followed user, a book they rated,
     *         and the rating score. The list contains up to 5 entries.
     */
    public List<Map<String, Object>> getBookRecommendation(ObjectId userId, List<String> languages) {
        try (Session session = graphConnector.getSession()) {
            String cypherQuery =
                    "MATCH (user:User {id: $userId})-[:FOLLOWS]->(other:User)-[r:RATES]->(book:Book) " +
                    "WHERE book.language IN $languages " +
                    "RETURN other, book, r.rating AS rating " +
                    "ORDER BY rating DESC " +
                    "LIMIT 10";
    
            Result result = session.run(cypherQuery, 
                                        parameters("userId", userId.toHexString(), 
                                                   "languages", languages));
            List<Map<String, Object>> results = new ArrayList<>();
    
            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Map<String, Object> followedUserRatedBook = new HashMap<>();
                followedUserRatedBook.put("user", record.get("other").asMap());
                followedUserRatedBook.put("book", record.get("book").asMap());
                followedUserRatedBook.put("rating", record.get("rating").asInt());
                results.add(followedUserRatedBook);
            }
    
            return results;
        }
    }

    
    /**
     * Retrieves a list of the 10 most similar users to a given user based on the number of commonly 
     * rated books with a rating difference of less than 2. The users must share at least one favorite genre.
     *
     * @param userId The ObjectId of the user for whom the similar users are being identified.
     * @return A list of maps where each map represents a similar user, their nickname, 
     *         the titles of common books both users rated similarly, and the count of such common books. 
     *         The list contains up to 10 entries, ordered by the number of common books in descending order.
     */
    public List<Map<String, Object>> getUsersWithSimilarTastes(ObjectId userId) {
        try (Session session = graphConnector.getSession()) {
            String cypherQuery =
                    "MATCH (u1:User {id: $userId})-[r1:RATES]->(book:Book)<-[r2:RATES]-(u2:User) " +
                    "WHERE id(u1) <> id(u2) " +
                    "AND abs(r1.rating - r2.rating) < 2 " +
                    "WITH u1, u2, COLLECT(DISTINCT book.title) AS commonBooks, COUNT(book) AS commonBookCount " +
                    "WHERE SIZE([genre IN u1.favouriteGenres WHERE genre IN u2.favouriteGenres]) > 0 " +
                    "RETURN u2.id AS user2, u2.nickname AS Nickname, commonBooks, commonBookCount " +
                    "ORDER BY commonBookCount DESC " +
                    "LIMIT 10";
    
            Result result = session.run(cypherQuery, parameters("userId", userId.toHexString()));
            List<Map<String, Object>> results = new ArrayList<>();
    
            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Map<String, Object> userWithSimilarTastes = new HashMap<>();
                userWithSimilarTastes.put("user2", record.get("user2").asString());
                userWithSimilarTastes.put("Nickname", record.get("Nickname").asString());
                userWithSimilarTastes.put("commonBooks", record.get("commonBooks").asList(Value::asString));
                userWithSimilarTastes.put("commonBookCount", record.get("commonBookCount").asInt());
                results.add(userWithSimilarTastes);
            }
    
            return results;
        }
    }
    
    /**
     * Retrieves a list of 5 recent follows made by users followed by the specified user,
     * ordered in descending order based on the follow relationship ID.
     *
     * @param userId The ObjectId of the user for whom the recent follows are being retrieved.
     * @return A list of maps where each map represents a follow action, including the followed user's ID,
     *         the followed user's nickname, the ID of the user who was followed, the nickname of the user who was followed,
     *         and the follow relationship ID. The list contains up to 5 entries, ordered by the follow relationship ID in descending order.
     */

    //CREATE INDEX user_id_index FOR (u:User) ON (u.id);
    public List<Map<String, Object>> getRecentFollowsByFollowedUsers(ObjectId userId) {
        try (Session session = graphConnector.getSession()) {
            String cypherQuery =
                    "MATCH (user:User {id: $userId})-[:FOLLOWS]->(followed:User)-[f:FOLLOWS]->(other:User) " +
                    "RETURN followed.id AS followedUserId, followed.nickname AS followedUserNickname, " +
                    "other.id AS followedUserFollowId, other.nickname AS followedUserFollowNickname, id(f) AS followId " +
                    "ORDER BY id(f) DESC " +
                    "LIMIT 6";
    
            Result result = session.run(cypherQuery, parameters("userId", userId.toHexString()));
            List<Map<String, Object>> results = new ArrayList<>();
    
            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Map<String, Object> follow = new HashMap<>();
                follow.put("followedUserId", record.get("followedUserId").asString());
                follow.put("followedUserNickname", record.get("followedUserNickname").asString());
                follow.put("followedUserFollowId", record.get("followedUserFollowId").asString());
                follow.put("followedUserFollowNickname", record.get("followedUserFollowNickname").asString());
                follow.put("followId", record.get("followId"));
                results.add(follow);
            }
    
            return results;
        }
    }

    /**
     * Retrieves a list of 5 recent ratings made by users followed by the specified user,
     * ordered in descending order based on the rating ID.
     *
     * @param userId The ObjectId of the user for whom the recent ratings are being retrieved.
     * @return A list of maps where each map represents a rating, including the followed user's ID,
     *         the followed user's nickname, the book ID, the book title, the rating given, and the rating ID.
     *         The list contains up to 5 entries, ordered by the rating ID in descending order.
     */
    public List<Map<String, Object>> getRecentRatingsByFollowedUsers(ObjectId userId) {
        try (Session session = graphConnector.getSession()) {
            String cypherQuery =
                    "MATCH (user:User {id: $userId})-[:FOLLOWS]->(followed:User)-[r:RATES]->(book:Book) " +
                    "RETURN followed.id AS followedUserId, followed.nickname AS followedUserNickname, " +
                    "book.id AS bookId, book.title AS bookTitle, r.rating AS rating, id(r) AS ratingId " +
                    "ORDER BY id(r) DESC " +
                    "LIMIT 6";
    
            Result result = session.run(cypherQuery, parameters("userId", userId.toHexString()));
            List<Map<String, Object>> results = new ArrayList<>();
    
            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                Map<String, Object> rating = new HashMap<>();
                rating.put("followedUserId", record.get("followedUserId").asString());
                rating.put("followedUserNickname", record.get("followedUserNickname").asString());
                rating.put("bookId", record.get("bookId").asString());
                rating.put("bookTitle", record.get("bookTitle").asString());
                rating.put("rating", record.get("rating").asInt());
                rating.put("ratingId", record.get("ratingId"));
                results.add(rating);
            }
    
            return results;
        }
    }
    
}
