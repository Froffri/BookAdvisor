package it.unipi.lsmsdb.bookadvisor.model.user;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;

public class Reviewer extends User {
    private String nationality;
    private List<String> favouriteGenres;
    private List<String> spokenLanguages;
    private List<ObjectId> upVotedReviews;
    private List<ObjectId> downVotedReviews;
    private List<ObjectId> reviewIds; 

    // Default constructor
    public Reviewer() {
        super();
    }

    // Parameterized constructor
    public Reviewer(ObjectId id, String name, String nickname, String password, LocalDate birthdate,
                          String gender, String nationality, List<String> favouriteGenres, List<String> spokenLanguages) {
        super(id, name, nickname, password, birthdate, gender);
        this.nationality = nationality;
        this.favouriteGenres = favouriteGenres;
        this.spokenLanguages = spokenLanguages;
        this.downVotedReviews = null;
        this.upVotedReviews = null;
        this.reviewIds = null; // Initialize reviewIds
    }

    public Reviewer(String name, String nickname, String password, LocalDate birthdate,
                          String gender, String nationality, List<String> favouriteGenres, List<String> spokenLanguages) {
        super(name, nickname, password, birthdate, gender);
        this.nationality = nationality;
        this.favouriteGenres = favouriteGenres;
        this.spokenLanguages = spokenLanguages;
        this.downVotedReviews = null;
        this.upVotedReviews = null;
        this.reviewIds = null; // Initialize reviewIds
    }

    // Constructor from MongoDB Document
    public Reviewer(Document doc) {
        super(doc);
        this.nationality = doc.getString("nationality");
        this.favouriteGenres = doc.getList("favouriteGenres", String.class);
        this.spokenLanguages = doc.getList("spokenLanguages", String.class);
        this.downVotedReviews = doc.getList("downVotedReviews", ObjectId.class);
        this.upVotedReviews = doc.getList("upVotedReviews", ObjectId.class);
        this.reviewIds = doc.getList("reviewIds", ObjectId.class); // Extract reviewIds
    }    
    
    // Constructor from Neo4j Node
    public Reviewer(Node node) {
        super(node);
        this.favouriteGenres = node.get("favouriteGenres").asList(Value::asString);
        this.spokenLanguages = null;
        this.downVotedReviews = null;
        this.upVotedReviews = null;
        this.reviewIds = null; 
    }

    // Getters and Setters

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public List<String> getFavouriteGenres() {
        return favouriteGenres;
    }

    public String getFavouriteGenresString() {
        String genres = "[";
        for (String genre : favouriteGenres) {
            genres += "'" + genre + "', ";
        }
        return genres.substring(0, genres.length() - 2) + "]";
    }

    public void setFavouriteGenres(List<String> favouriteGenres) {
        this.favouriteGenres = favouriteGenres;
    }

    public List<String> getSpokenLanguages() {
        return spokenLanguages;
    }

    public String getSpokenLanguagesString() {
        String languages = "[";
        for (String language : spokenLanguages) {
            languages += "'" + language + "', ";
        }
        return languages.substring(0, languages.length() - 2) + "]";
    }

    public void setSpokenLanguages(List<String> spokenLanguages) {
        this.spokenLanguages = spokenLanguages;
    }

    public List<ObjectId> getUpVotedReviews() {
        return upVotedReviews;
    }

    public void setUpVotedReviews(List<ObjectId> upVotedReviews) {
        this.upVotedReviews = upVotedReviews;
    }

    public void addUpVotedReview(ObjectId reviewId) {
        if (this.upVotedReviews == null) {
            this.upVotedReviews = new ArrayList<>();
        }
        this.upVotedReviews.add(reviewId);
    }

    public void removeUpVotedReview(ObjectId reviewId) {
        if (this.upVotedReviews != null) {
            this.upVotedReviews.remove(reviewId);
        }
    }

    public List<ObjectId> getDownVotedReviews() {
        return downVotedReviews;
    }

    public void setDownVotedReviews(List<ObjectId> downVotedReviews) {
        this.downVotedReviews = downVotedReviews;
    }

    public void addDownVotedReview(ObjectId reviewId) {
        if (this.downVotedReviews == null) {
            this.downVotedReviews = new ArrayList<>();
        }
        this.downVotedReviews.add(reviewId);
    }

    public void removeDownVotedReview(ObjectId reviewId) {
        if (this.downVotedReviews != null) {
            this.downVotedReviews.remove(reviewId);
        }
    }

    public List<ObjectId> getReviewIds() {
        return reviewIds;
    }

    public void setReviewIds(List<ObjectId> reviewIds) {
        this.reviewIds = reviewIds;
    }

    // toString method for debugging and logging
    @Override
    public String toString() {
        return "Reviewer{" +
                "nationality='" + nationality + '\'' +
                ", favouriteGenres=" + favouriteGenres +
                ", spokenLanguages=" + spokenLanguages +
                ", upVotedReviews=" + upVotedReviews +
                ", downVotedReviews=" + downVotedReviews +
                ", reviewIds=" + reviewIds +
                "} " + super.toString();
    }

    // toDocument method for MongoDB
    @Override
    public Document toDocument() {
        Document doc = super.toDocument();
        doc.append("nationality", nationality)
        .append("favouriteGenres", favouriteGenres)
        .append("spokenLanguages", spokenLanguages)
        .append("upVotedReviews", upVotedReviews)
        .append("downVotedReviews", downVotedReviews)
        .append("reviewIds", reviewIds);
        return doc;
    }

    // CRUD operations for reviews

    // Add a review ID to the reviewIds list
    public void addReview(ObjectId reviewId) {
        if (this.reviewIds == null) {
            this.reviewIds = new ArrayList<>();
        }
        this.reviewIds.add(reviewId);
    }

    // Remove a review ID from the reviewIds list
    public void removeReview(ObjectId reviewId) {
        if (this.reviewIds != null) {
            this.reviewIds.remove(reviewId);
        }
    }

    // Update a review ID in the reviewIds list
    public void updateReview(ObjectId oldReviewId, ObjectId newReviewId) {
        if (this.reviewIds != null) {
            int index = this.reviewIds.indexOf(oldReviewId);
            if (index != -1) {
                this.reviewIds.set(index, newReviewId);
            }
        }
    }

    // Get all review IDs
    public List<ObjectId> getAllReviews() {
        return this.reviewIds;
    }
}
