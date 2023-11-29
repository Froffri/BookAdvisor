package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDao {
    private static final String COLLECTION_NAME = "books";
    private MongoCollection<Document> collection;

    public BookDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        collection = database.getCollection(COLLECTION_NAME);
    }

    // Insert a new book into the database
    public void insertBook(Book book) {
        Document doc = new Document("title", book.getTitle())
            .append("author", book.getAuthor())
            .append("genre", book.getGenre())
            .append("year", book.getYear())
            .append("language", book.getLanguage())
            .append("numPages", book.getNumPages())
            .append("sumStars", book.getSumStars())
            .append("numRatings", book.getNumRatings());
        collection.insertOne(doc);
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

    // Update a book's information
    public void updateBook(String id, Book book) {
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
    }

    // Delete a book from the database
    public void deleteBook(String id) {
        collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
    }
}
