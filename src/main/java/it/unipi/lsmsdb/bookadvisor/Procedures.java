package it.unipi.lsmsdb.bookadvisor;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.reactivestreams.client.MongoCollection;

import it.unipi.lsmsdb.bookadvisor.model.book.Book;


public class Procedures {
    // functions that finds all books reviewed by users with the given input nationality
    // First get the ids of the users with the given nationality
    // Then I use the users in the rating table to get the books reviewed by the users with the given
    // then I use the bookIds found to get the books from the book table
    public static final List<Book> getBooksReviewedByUsersWithInputNationality(String nationality){
        List<Book> books = new ArrayList<>();
        try{
            // get the ids of the users with the given nationality
            List<ObjectId> userIds = new ArrayList<>();
            MongoCollection<Document> usersCollection = DatabaseHandler.getUsersCollection();
            FindIterable<Document> users = usersCollection.find

            // get the books reviewed by the users with the
            MongoCollection<Document> ratingsCollection = DatabaseHandler.getRatingsCollection();
            FindIterable<Document> ratings = ratingsCollection.find(Filters.in("userId", userIds));
            List<ObjectId> bookIds = new ArrayList<>();
            for(Document rating : ratings){
                bookIds.add(rating.getObjectId("bookId"));
            }

            // get the books from the book table
            MongoCollection<Document> booksCollection = DatabaseHandler.getBooksCollection();
            FindIterable<Document> booksDocuments = booksCollection.find(Filters.in("_id", bookIds));
            for(Document bookDocument : booksDocuments){
                books.add(new Book(bookDocument));
            }
        }catch(Exception e){
            e.printStackTrace();

        }
    }

    
}
