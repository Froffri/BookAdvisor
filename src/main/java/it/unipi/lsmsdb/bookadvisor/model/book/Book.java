package it.unipi.lsmsdb.bookadvisor.model.book;

import org.bson.Document;
import org.bson.types.ObjectId;

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

    // Method to convert a Book object to a Document
    public Document toDocument() {
        return new Document("title", title)
                .append("author", author)
                .append("genre", genre)
                .append("year", year)
                .append("language", language)
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
}
