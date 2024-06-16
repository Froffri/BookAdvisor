package it.unipi.lsmsdb.bookadvisor;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.AggregateIterable;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.*;
import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Projections.computed;
import static com.mongodb.client.model.Projections.fields;

import it.unipi.lsmsdb.bookadvisor.model.book.Book;


public class Procedures {

    public void runAggregation(ObjectId authorId) {
        // Connect to MongoDB
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            // Get the database and collection
            MongoDatabase database = mongoClient.getDatabase("your_database_name");
            MongoCollection<Document> collection = database.getCollection("books");

            // Define the aggregation pipeline stages
            AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(
                    // Match stage
                    new Document("$match", new Document("author", authorId)),

                    // AddFields stage for averageRating
                    new Document("$addFields", new Document("averageRating",
                            new Document("$divide", Arrays.asList("$sumStars", "$numRatings")))),

                    // AddFields stage for countryRatings
                    new Document("$addFields", new Document("countryRatings",
                            new Document("$map", new Document("input",
                                    new Document("$objectToArray", "$ratingsAggByNat"))
                                    .append("as", "rating")
                                    .append("in", new Document("country", "$$rating.k")
                                            .append("data", new Document("averageRating",
                                                    new Document("$divide", Arrays.asList("$$rating.v.sumRating", "$$rating.v.cardinality")))
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

            // Iterate and process the results
            for (Document doc : iterable) {
                System.out.println(doc.toJson());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void findMostFamousBooks(String genre) {
        // Connect to MongoDB
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            // Get the database and collection
            MongoDatabase database = mongoClient.getDatabase("your_database_name");
            MongoCollection<Document> collection = database.getCollection("books");

            // Define the aggregation pipeline stages
            AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(
                    // Match stage
                    new Document("$match", new Document("genre", genre)),

                    // Project stage
                    new Document("$project", new Document("title", 1)
                            .append("ratingsAggByNat", 1)
                            .append("countryReviews", new Document("$map", new Document("input",
                                    new Document("$objectToArray", "$ratingsAggByNat"))
                                    .append("as", "country")
                                    .append("in", new Document("country", "$$country.k")
                                            .append("numReviews", "$$country.v.cardinality")
                                            .append("bookTitle", "$title")
                                            .append("bookId", "$_id"))))),

                    // Unwind stage
                    new Document("$unwind", "$countryReviews"),

                    // Group stage
                    new Document("$group", new Document("_id", "$countryReviews.country")
                            .append("mostFamousBook", new Document("$first", new Document("title", "$countryReviews.bookTitle")
                                    .append("bookId", "$countryReviews.bookId")
                                    .append("numReviews", "$countryReviews.numReviews")))
                            .append("maxNumReviews", new Document("$max", "$countryReviews.numReviews"))),

                    // Project stage for final result
                    new Document("$project", new Document("_id", 0)
                            .append("country", "$_id")
                            .append("title", "$mostFamousBook.title")
                            .append("bookId", "$mostFamousBook.bookId")
                            .append("numReviews", "$mostFamousBook.numReviews"))
            ));

            // Iterate and process the results
            for (Document doc : iterable) {
                System.out.println(doc.toJson());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void findMostUsefulReviews(String username) {
        // Connect to MongoDB
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            // Get the database and collection
            MongoDatabase database = mongoClient.getDatabase("test");
            MongoCollection<Document> collection = database.getCollection("books");

            // Define the aggregation pipeline stages
            AggregateIterable<Document> iterable = collection.aggregate(Arrays.asList(
                    // Match stage
                    new Document("$match", new Document("most10UsefulReviews.userName", username)),

                    // AddFields stage to calculate review usefulness
                    new Document("$addFields", new Document("most10UsefulReviews",
                            new Document("$map", new Document("input", "$most10UsefulReviews")
                                    .append("as", "review")
                                    .append("in", new Document("$mergeObjects", Arrays.asList(
                                            "$$review",
                                            new Document("usefulness",
                                                    new Document("$subtract", Arrays.asList("$$review.countUpVote", "$$review.countDownVote")))
                                    )))))),

                    // Filter stage to include reviews by the given username
                    new Document("$addFields", new Document("most10UsefulReviews",
                            new Document("$filter", new Document("input", "$most10UsefulReviews")
                                    .append("as", "review")
                                    .append("cond", new Document("$eq", Arrays.asList("$$review.userName", username)))))),

                    // Sort stage by usefulness in descending order
                    new Document("$addFields", new Document("most10UsefulReviews",
                            new Document("$slice", Arrays.asList(
                                    new Document("$sortArray",
                                            new Document("input", "$most10UsefulReviews")
                                                    .append("sortBy", new Document("usefulness", -1))),
                                    3)))),

                    // Project stage to output desired fields
                    new Document("$project", new Document("_id", 0)
                            .append("bookTitle", "$title")
                            .append("author", 1)
                            .append("language", 1)
                            .append("year", 1)
                            .append("genre", 1)
                            .append("imageUrl", 1)
                            .append("numPages", 1)
                            .append("mostUsefulReviews", "$most10UsefulReviews"))
            ));

            // Iterate and process the results
            for (Document doc : iterable) {
                System.out.println(doc.toJson());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String username = "AliceJones89"; // Replace with the desired username
        new Procedures().findMostUsefulReviews(username);
    }

    
}
