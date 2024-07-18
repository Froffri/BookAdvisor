package it.unipi.lsmsdb.bookadvisor.model.user;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.neo4j.driver.Value;
import org.neo4j.driver.types.Node;

public class Reviewer {
    private ObjectId id;
    private String name;
    private String nickname;
    private String password;
    private LocalDate birthdate;
    private String gender;
    private String nationality;
    private List<String> favouriteGenres;
    private List<String> spokenLanguages;
    private List<ObjectId> upVotedReviews;
    private List<ObjectId> downVotedReviews;

    // Default constructor
    public Reviewer() {
        
    }

    // Parameterized constructor
    public Reviewer(ObjectId id, String name, String nickname, String password, LocalDate birthdate,
                          String gender, String nationality, List<String> favouriteGenres, List<String> spokenLanguages) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.birthdate = birthdate;
        this.gender = gender;
        this.nationality = nationality;
        this.favouriteGenres = favouriteGenres;
        this.spokenLanguages = spokenLanguages;
        this.downVotedReviews = new ArrayList<>();
        this.upVotedReviews = new ArrayList<>();
    }

    public Reviewer(String name, String nickname, String password, LocalDate birthdate,
                          String gender, String nationality, List<String> favouriteGenres, List<String> spokenLanguages) {
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.birthdate = birthdate;
        this.gender = gender;
        this.nationality = nationality;
        this.favouriteGenres = favouriteGenres;
        this.spokenLanguages = spokenLanguages;
        this.downVotedReviews = new ArrayList<>();
        this.upVotedReviews = new ArrayList<>();
    }

    // Constructor from MongoDB Document
    public Reviewer(Document doc) {
        this.id = doc.getObjectId("_id");
        this.name = doc.getString("name");
        this.nickname = doc.getString("nickname");
        this.password = doc.getString("password");
        this.gender = doc.getString("gender");
        
        // Conversione della stringa di data in LocalDate
        this.birthdate = LocalDate.parse(doc.getString("birth"));
        this.nationality = doc.getString("nationality");
        this.favouriteGenres = doc.getList("favourite_genres", String.class);
        this.spokenLanguages = doc.getList("spoken_languages", String.class);
        this.upVotedReviews = doc.getList("up_voted_reviews", ObjectId.class);
        this.downVotedReviews = doc.getList("down_voted_reviews", ObjectId.class);
    }    
    
    // Constructor from Neo4j Node
    public Reviewer(Node node) {
        this.id = new ObjectId(node.get("id").asString());
        this.name = null;
        this.nickname = node.get("nickname").asString();
        this.password = null;
        this.birthdate = null;
        this.gender = null; 
        this.favouriteGenres = node.get("favourite_genres").asList(Value::asString);
        this.spokenLanguages = null;
        this.downVotedReviews = null;
        this.upVotedReviews = null;
    }

    // Getters and Setters

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

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

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

    // toString method for debugging and logging
    @Override
    public String toString() {
        return "Reviewer{" +
                "_id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", birth='" + birthdate + '\'' +
                ", gender='" + gender + '\'' +
                ", nationality='" + nationality + '\'' +
                ", favouriteGenres=" + favouriteGenres +
                ", spokenLanguages=" + spokenLanguages +
                ", upVotedReviews=" + upVotedReviews +
                ", downVotedReviews=" + downVotedReviews +
                "} " + super.toString();
    }

    // toDocument method for MongoDB
    public Document toDocument() {
        return new Document("name", name)
                .append("nickname", nickname)
                .append("password", password)
                .append("birth", birthdate.toString())
                .append("gender", gender)
                .append("nationality", nationality)
                .append("favourite_genres", favouriteGenres)
                .append("spoken_languages", spokenLanguages)
                .append("up_voted_reviews", upVotedReviews)
                .append("down_voted_reviews", downVotedReviews);
    }
}
