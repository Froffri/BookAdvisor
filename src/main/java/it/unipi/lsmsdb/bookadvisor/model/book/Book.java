package it.unipi.lsmsdb.bookadvisor.model.book;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.types.Node;

import it.unipi.lsmsdb.bookadvisor.utils.RatingAggregate;
import it.unipi.lsmsdb.bookadvisor.model.review.Review;
  

public class Book {

    public static class Author {
        private ObjectId id;
        private String name;

        // Costruttore
        public Author(ObjectId id, String name) {
            this.id = id;
            this.name = name;
        }
        public Author(Author author) {
            this.id = author.id;
            this.name = author.name;
        }

        // Getter e Setter
        public ObjectId getId() {
            return id;
        }

        public void setId(ObjectId id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Author{id=" + id + ", name='" + name + "'}";
        }
        public Document toDocument() {
            return new Document("id", id).append("name", name);
        }
    }

    private ObjectId id;
    private int sumStars;
    private int numRatings;
    private String language;
    private String title;
    private Author[] authors;
    private String[] genre;
    private int year;
    private String imageUrl;
    private int numPages;
    private List<ObjectId> reviewIds;
    private Map<String, RatingAggregate> ratingsAggByNat;
    private List<Review> most10UsefulReviews;

    // Constructor
    public Book(ObjectId id, int sumStars, int numRatings, String language,
                String title, Author[] authors, String[] genre, int year,
                String imageUrl, int numPages, List<ObjectId> reviewIds, Map<String, RatingAggregate> ratingsAggByNat, List<Review> most10UsefulReviews) {
        this.id = id;
        this.sumStars = sumStars;
        this.numRatings = numRatings;
        this.language = language;
        this.title = title;
        this.authors = new Author[authors.length];

        for (int i = 0; i < authors.length; i++) {
            this.authors[i] = new Author(authors[i]);
        }

        this.genre = genre.clone();
        this.year = year;
        this.imageUrl = imageUrl;
        this.numPages = numPages;
        this.reviewIds = new ArrayList<ObjectId>(reviewIds);
        this.ratingsAggByNat = ratingsAggByNat;
        this.most10UsefulReviews = most10UsefulReviews;
    }

    // Constructor that accepts a Document object
    public Book(Document doc) {
        this.id = doc.getObjectId("_id");
        this.sumStars = doc.getInteger("sumStars");
        this.numRatings = doc.getInteger("numRatings");
        this.language = doc.getString("language");
        this.title = doc.getString("title");

        this.authors = doc.getList("authors", Document.class).stream()
                .map(authorDoc -> new Author(authorDoc.getObjectId("id"), authorDoc.getString("name")))
                .toArray(Author[]::new);

        this.genre = doc.getList("genre", String.class).toArray(new String[0]);
        this.year = doc.getInteger("year");
        this.imageUrl = doc.getString("image_url");
        this.numPages = doc.getInteger("num_pages");
        this.reviewIds = doc.getList("review_ids", ObjectId.class);
        this.ratingsAggByNat = (Map<String, RatingAggregate>) doc.get("ratings_agg_by_nat");

        List<Document> reviewsList = (List<Document>) doc.get("most_10_useful_reviews");
        if (reviewsList == null) {
            this.most10UsefulReviews = null;
        } else {
            this.most10UsefulReviews = new ArrayList<>();
            for (Document reviewDoc : reviewsList) {
                Review review = new Review(reviewDoc);
                this.most10UsefulReviews.add(review);
            }
        }
    }

    // Constructor that accepts a Neo4j Node object
    public Book(Node node) {
        this.id = new ObjectId(node.get("id").asString());
        this.sumStars = 0;
        this.numRatings = 0;
        this.language = node.get("language").asString();
        this.title = node.get("title").asString();
        this.authors = null;
        this.genre = null;
        this.year = 0;
        this.imageUrl = null;
        this.numPages = 0;
        this.reviewIds = null;
        this.ratingsAggByNat = null;
        this.most10UsefulReviews = null;
    }

    public Book(){
        this.id = null;
        this.sumStars = 0;
        this.numRatings = 0;
        this.language = null;
        this.title = null;
        this.authors = null;
        this.genre = null;
        this.year = 0;
        this.imageUrl = null;
        this.numPages = 0;
        this.reviewIds = null;
        this.ratingsAggByNat = null;
        this.most10UsefulReviews = null;
    }

    // Method to convert a Book object to a Document
    public Document toDocument() {
        List<Document> authorDocs = new ArrayList<>();
        for (Author author : authors) {
            authorDocs.add(author.toDocument());
        }

        return new Document("title", title)
                .append("authors", authorDocs)
                .append("genre", Arrays.asList(genre))
                .append("year", year)
                .append("language", language)
                .append("num_pages", numPages)
                .append("sumStars", sumStars)
                .append("numRatings", numRatings)
                .append("image_url", imageUrl)
                .append("review_ids", reviewIds)
                .append("ratings_agg_by_nat", ratingsAggByNat)
                .append("most_10_useful_reviews", most10UsefulReviews);
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

    public Author[] getAuthors() {
        return authors;
    }
    
    public void setAuthors(Author[] authors) {
        this.authors = authors;
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

    public List<ObjectId> getReviewIds() {
        return reviewIds;
    }

    public void setReviewIds(List<ObjectId> reviewIds) {
        this.reviewIds = reviewIds;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Book{")
                .append("id=").append(id)
                .append(", sumStars=").append(sumStars)
                .append(", numRatings=").append(numRatings)
                .append(", language='").append(language).append('\'')
                .append(", title='").append(title).append('\'')
                .append(", authors=[");
        for (int i = 0; i < authors.length; i++) {
            sb.append(authors[i].getName());
            if (i != authors.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]")
                .append(", genre=").append(Arrays.toString(genre))
                .append(", year=").append(year)
                .append(", imageUrl='").append(imageUrl).append('\'')
                .append(", numPages=").append(numPages)
                .append(", reviewIds=").append(reviewIds)
                .append(", ratingsAggByNat=").append(ratingsAggByNat)
                .append(", most10UsefulReviews=").append(most10UsefulReviews)
                .append('}');
        return sb.toString();
    }

    public void addRating(String nat, int rating) {
        RatingAggregate ratingAggregate = ratingsAggByNat.get(nat);
        if (ratingAggregate == null) {
            ratingAggregate = new RatingAggregate();
            ratingsAggByNat.put(nat, ratingAggregate);
        }
        ratingAggregate.addRating(rating);
    }

    public void removeRating(String nat, int rating) {
        RatingAggregate ratingAggregate = ratingsAggByNat.get(nat);
        if (ratingAggregate != null) {
            ratingAggregate.removeRating(rating);
        }
    }

    public void updateRating(String nat, int oldRating, int newRating) {
        RatingAggregate ratingAggregate = ratingsAggByNat.get(nat);
        if (ratingAggregate != null) {
            ratingAggregate.updateRating(oldRating, newRating);
        }
    }

    public void addReview(ObjectId reviewId) {
        if (this.reviewIds == null) {
            this.reviewIds = new ArrayList<>();
        }
        if (!this.reviewIds.contains(reviewId)) {
            this.reviewIds.add(reviewId);
        }
    }

    public void insertReview(ObjectId id, ObjectId userId, ObjectId bookId, String nickname, String text, String nat,  int stars, int countUpVote, int countDownVote) {
        Review review = new Review(id, userId, bookId, nickname, text, nat, stars, countUpVote, countDownVote);
        most10UsefulReviews.add(review);
    }

    public void removeReview(ObjectId reviewId) {
        for (Review review : most10UsefulReviews) {
            if (review.getId().equals(reviewId)) {
                most10UsefulReviews.remove(review);
                break;
            }
        }
    }

    public void updateReview(ObjectId reviewId, String text, int stars) {
        for (Review review : most10UsefulReviews) {
            if (review.getId().equals(reviewId)) {
                review.setText(text);
                review.setStars(stars);
                break;
            }
        }
    }

    public void updateReviewVotes(ObjectId reviewId, int countUpVote, int countDownVote) {
        for (Review review : most10UsefulReviews) {
            if (review.getId().equals(reviewId)) {
                review.setCountUpVote(countUpVote);
                review.setCountDownVote(countDownVote);
                break;
            }
        }
    }

}
