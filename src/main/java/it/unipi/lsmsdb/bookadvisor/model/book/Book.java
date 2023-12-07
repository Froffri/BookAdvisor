package it.unipi.lsmsdb.bookadvisor.model.book;

import org.bson.Document;

public class Book {
    private String id;
    private int sumStars;
    private int numRatings;
    private String language;
    private String title;
    private String author;
    private String[] genre;
    private int year;
    private String imageUrl;
    private int numPages;

    // Constructor
    public Book(String id, int sumStars, int numRatings, String language, 
                String title, String author, String[] genre, int year, 
                String imageUrl, int numPages) {
        // Initialize fields
    }

    // Constructor that accepts a Document object
    public Book(Document doc) {
        // Initialize fields from Document
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
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
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