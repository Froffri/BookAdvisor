package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import it.unipi.lsmsdb.bookadvisor.model.book.Book;

import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class BookDao {
    private static final String COLLECTION_NAME = "books";
    private MongoCollection<Document> collection;

    // Constructor
    public BookDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        collection = database.getCollection(COLLECTION_NAME);
    }

    // Find a book by its ID
    public Optional<Book> findBookById(String id) {
        Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
        return Optional.ofNullable(doc).map(Book::new);
    }

    // Find books by title
    public List<Book> findBooksByTitle(String title) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("title", title))) {
            books.add(new Book(doc));
        }
        return books;
    }

    // Insert a new book into the database
    public void insertBook(Book book) {
        try {
            Document doc = book.toDocument();
            collection.insertOne(doc);
        } catch (Exception e) {
            System.err.println("Errore durante l'inserimento del libro: " + e.getMessage());
        }
    }

    // Update a book's information
    public void updateBook(String id, Book book) {
        try {
            collection.updateOne(Filters.eq("_id", new ObjectId(id)),
                Updates.combine(
                    Updates.set("author", book.getAuthor()),
                    Updates.set("genre", book.getGenre()),
                    Updates.set("year", book.getYear()),
                    Updates.set("language", book.getLanguage()),
                    Updates.set("numPages", book.getNumPages()),
                    Updates.set("sumStars", book.getSumStars()),
                    Updates.set("numRatings", book.getNumRatings())
                ));
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento del libro: " + e.getMessage());
        }
    }

    // Delete a book from the database
    public void deleteBook(String id) {
        try {
            collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
        } catch (Exception e) {
            System.err.println("Errore durante la cancellazione del libro: " + e.getMessage());
        }
    }

    // Update the rating of a book
    protected void updateBookRating(ObjectId bookId, int rating) {
        try {
            Document book = collection.find(Filters.eq("_id", bookId)).first();
            if (book != null) {
                int sumStars = book.getInteger("sumStars", 0) + rating;
                int numRatings = book.getInteger("numRatings", 0) + 1;
                collection.updateOne(Filters.eq("_id", bookId), 
                                         Updates.combine(Updates.set("sumStars", sumStars),
                                                         Updates.set("numRatings", numRatings)));
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento dei rating del libro: " + e.getMessage());
        }
    }
}
