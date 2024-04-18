package it.unipi.lsmsdb.bookadvisor.utils;

public class RatingAggregate {
    private int sumRating;
    private int cardinality;

    // Constructors, getters, and setters
    public RatingAggregate() {
    }

    public RatingAggregate(int sumRating, int cardinality) {
        this.sumRating = sumRating;
        this.cardinality = cardinality;
    }

    public int getSumRating() {
        return sumRating;
    }

    public void setSumRating(int sumRating) {
        this.sumRating = sumRating;
    }

    public int getCardinality() {
        return cardinality;
    }

    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }

    public double getAverageRating() {
        return (double) sumRating / cardinality;
    }

    public void addRating(int rating) {
        sumRating += rating;
        cardinality++;
    }

    public void removeRating(int rating) {
        sumRating -= rating;
        cardinality--;
    }

    public void updateRating(int oldRating, int newRating) {
        sumRating = sumRating - oldRating + newRating;
    }


}

