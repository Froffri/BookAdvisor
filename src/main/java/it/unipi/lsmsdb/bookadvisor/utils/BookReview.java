package it.unipi.lsmsdb.bookadvisor.utils;

public class BookReview {
    private int id;
    private String body;
    private String lang;
    private int nUpvotes;
    private int nDownvotes;

    // Constructors, getters, and setters
    public BookReview() {
    }

    public BookReview(int id, String body, String lang, int nUpvotes, int nDownvotes) {
        this.id = id;
        this.body = body;
        this.lang = lang;
        this.nUpvotes = nUpvotes;
        this.nDownvotes = nDownvotes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public int getNUpvotes() {
        return nUpvotes;
    }

    public void setNUpvotes(int nUpvotes) {
        this.nUpvotes = nUpvotes;
    }

    public int getNDownvotes() {
        return nDownvotes;
    }

    public void setNDownvotes(int nDownvotes) {
        this.nDownvotes = nDownvotes;
    }

    public void upvote() {
        nUpvotes++;
    }

    public void downvote() {
        nDownvotes++;
    }

    public void removeUpvote() {
        nUpvotes--;
    }

    public void removeDownvote() {
        nDownvotes--;
    }

    public double getRating() {
        return (double) (nUpvotes - nDownvotes);
    }

    public void updateRating(int oldVote, int newVote) {
        nUpvotes = nUpvotes - oldVote + newVote;
        nDownvotes = nDownvotes + oldVote - newVote;
    }
}
