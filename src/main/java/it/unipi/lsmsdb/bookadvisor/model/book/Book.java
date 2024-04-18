package it.unipi.lsmsdb.bookadvisor.model.book;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.types.Node;

import it.unipi.lsmsdb.bookadvisor.utils.RatingAggregate;
import it.unipi.lsmsdb.bookadvisor.model.review.Review;
  

public class Book {
    private ObjectId id;
    private int sumStars;
    private int numRatings;
    private String language;
    private String title;
    private ObjectId[] author;
    private String[] genre;
    private int year;
    private String imageUrl;
    private int numPages;
    private Map<String, RatingAggregate> ratingsAggByNat;
    private List<Review> most10UsefulReviews;

    // Constructor
    public Book(ObjectId id, int sumStars, int numRatings, String language, 
                String title, ObjectId[] author, String[] genre, int year, 
                String imageUrl, int numPages) {
        this.id = id;
        this.sumStars = sumStars;
        this.numRatings = numRatings;
        this.language = language;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
        this.imageUrl = imageUrl;
        this.numPages = numPages;
    }

    // Constructor that accepts a Document object
    public Book(Document doc) {
        this.id = doc.getObjectId("_id");
        this.sumStars = doc.getInteger("sumStars");
        this.numRatings = doc.getInteger("numRatings");
        this.language = doc.getString("language");
        this.title = doc.getString("title");
        this.author = (ObjectId[]) doc.get("author");
        this.genre = (String[]) doc.get("genre");
        this.year = doc.getInteger("year");
        this.imageUrl = doc.getString("imageUrl");
        this.numPages = doc.getInteger("numPages");
    }

    // Constructor that accepts a Neo4j Node object
    public Book(Node node) {
        this.id = new ObjectId(node.get("id").asString());
        this.sumStars = node.get("sumStars").asInt();
        this.numRatings = node.get("numRatings").asInt();
        this.language = node.get("language").asString();
        this.title = null;
        this.author = null;
        this.genre = node.get("genre").asList(value -> value.asString()).toArray(new String[0]);
        this.year = 0;
        this.imageUrl = null;
        this.numPages = 0;
    }

    // Method to convert a Book object to a Document
    public Document toDocument() {
        return new Document("title", title)
                .append("author", author)
                .append("genre", genre)
                .append("year", year)
                .append("lodel.review.Review;anguage", language)
                .append("numPages", numPages)
                .append("sumStars", sumStars)
                .append("numRatings", numRatings);
    }
    // Getters and setters
    
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public int getSumStars() {
        return sumStars;
    }

    public void setSumStars(int sumStars) {
        this.sumStars = sumStars;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public ObjectId[] getAuthor() {
        return author;
    }

    public void setAuthor(ObjectId[] author) {
        this.author = author;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getGenre() {
        return genre;
    }

    public void setGenre(String[] genre) {
        this.genre = genre;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getNumPages() {
        return numPages;
    }

    public void setNumPages(int numPages) {
        this.numPages = numPages;
    }

    public Map<String, RatingAggregate> getRatingsAggByNat() {
        return ratingsAggByNat;
    }

    public void setRatingsAggByNat(Map<String, RatingAggregate> reviewsAggByNat) {
        this.ratingsAggByNat = reviewsAggByNat;
    }

    public List<Review> getMost10UsefulReviews() {
        return most10UsefulReviews;
    }

    public void setMost10UsefulReviews(List<Review> most10UsefulReviews) {
        this.most10UsefulReviews = most10UsefulReviews;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", sumStars=" + sumStars +
                ", numRatings=" + numRatings +
                ", language='" + language + '\'' +
                ", title='" + title + '\'' +
                ", author=" + author +
                ", genre=" + genre +
                ", year=" + year +
                ", imageUrl='" + imageUrl + '\'' +
                ", numPages=" + numPages +
                '}';
    }

    public void addReview(String nat, int rating) {
        RatingAggregate ratingAggregate = ratingsAggByNat.get(nat);
        if (ratingAggregate == null) {
            ratingAggregate = new RatingAggregate();
            ratingsAggByNat.put(nat, ratingAggregate);
        }
        ratingAggregate.addRating(rating);
    }

    public void removeReview(String nat, int rating) {
        RatingAggregate ratingAggregate = ratingsAggByNat.get(nat);
        if (ratingAggregate != null) {
            ratingAggregate.removeRating(rating);
        }
    }

    public void updateReview(String nat, int oldRating, int newRating) {
        RatingAggregate ratingAggregate = ratingsAggByNat.get(nat);
        if (ratingAggregate != null) {
            ratingAggregate.updateRating(oldRating, newRating);
        }
    }

    public void addReview(String nat, int rating, String body, String lang, int nUpvotes, int nDownvotes) {
        BookReview review = new Review(0, body, lang, nUpvotes, nDownvotes);
        most10UsefulReviews.add(review);
        addReview(nat, rating);
    }


}
