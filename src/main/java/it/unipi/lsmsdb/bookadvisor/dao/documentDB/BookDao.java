package it.unipi.lsmsdb.bookadvisor.dao.documentDB;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import it.unipi.lsmsdb.bookadvisor.model.book.Book;

import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class BookDao {
    private static final String COLLECTION_NAME = "books";
    private MongoCollection<Document> collection;
    private UserDao userDao;

    // Constructor
    public BookDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        collection = database.getCollection(COLLECTION_NAME);
        this.userDao = new UserDao(connector);
    }

    // Find a book by its ID
    public Book findBookById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        if (doc != null) {
            return new Book(doc);
        } else {
            return null;
        }
    }

    // Find books by title
    public List<Book> findBooksByTitle(String title) {
        List<Book> books = new ArrayList<>();
        Pattern regex = Pattern.compile(title, Pattern.CASE_INSENSITIVE);
        for (Document doc : collection.find(Filters.regex("title", regex))) {
            books.add(new Book(doc));
        }
        return books;
    }

    // Insert a new book into the database
    public boolean addBook(Book book) {
        try {
            BsonValue insertedId = collection.insertOne(book.toDocument()).getInsertedId();
            ObjectId bookid = insertedId.asObjectId().getValue();
            book.setId(bookid);
        } catch (Exception e) {
            System.err.println("Errore durante l'inserimento del libro: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean addBook(ObjectId bookId, Book book) {
        try {
            // Insert the book with the given ID
            collection.insertOne(book.toDocument().append("_id", bookId));
        } catch (Exception e) {
            System.err.println("Errore durante l'inserimento del libro: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean addReviewToBook(ObjectId bookId, ObjectId reviewId) {
        try {
            collection.updateOne(Filters.eq("_id", bookId), Updates.addToSet("review_ids", reviewId));
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiunta della recensione al libro: " + e.getMessage());
            return false;
        }
        // Update the list of reviews
        
        
        return true;
    }

    public boolean removeReviewFromBook(ObjectId bookId, ObjectId reviewId) {
        try {
            collection.updateOne(Filters.eq("_id", bookId), Updates.pull("review_ids", reviewId));
        } catch (Exception e) {
            System.err.println("Errore durante la rimozione della recensione dal libro: " + e.getMessage());
            return false;
        }
        return true;
    }

    // Update a book's information
    public boolean updateBook(ObjectId bookId, Document book){
        try {
            collection.updateOne(Filters.eq("_id", bookId), new Document("$set", book));
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento del libro: " + e.getMessage());
            return false;
        }
        return true;
    }

    // Delete a book from the database
    public boolean deleteBook(ObjectId id) {
        try {
            collection.deleteOne(Filters.eq("_id", id));
        } catch (Exception e) {
            System.err.println("Errore durante la cancellazione del libro: " + e.getMessage());
            return false;
        }
        return true;
    }

    // Get book by its ID
    public Book getBookById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        return new Book(doc);
    }

    // Get all books by the title
    public List<Book> getBooksByTitle(String title) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("title", title))) {
            books.add(new Book(doc));
        }
        return books;
    }

    // Get all books of a given genre
    public List<Book> getBooksByGenre(String genre) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find(Filters.in("genre", genre))) {
            books.add(new Book(doc));
        }
        return books;
    }

    // Get all books of a given year
    public List<Book> getBooksByYear(int year) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("year", year))) {
            books.add(new Book(doc));
        }
        return books;
    }

    // Get all books of a given language
    public List<Book> getBooksByLanguage(String language) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("language", language))) {
            books.add(new Book(doc));
        }
        return books;
    }

    // Get all books with a certain rating
    public List<Book> getBooksByRating(double targetRating, boolean greaterOrEqual) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find()) {
            int sumStars = doc.getInteger("sumStars", 0); // 0 is a default value if sumStars is not present
            int numRatings = doc.getInteger("numRatings", 0); // 0 is a default value if numRatings is not present
    
            double currentRating = (numRatings > 0) ? (double) sumStars / numRatings : 0.0;
    
            if ((greaterOrEqual && currentRating >= targetRating) || (!greaterOrEqual && currentRating < targetRating)) {
                books.add(new Book(doc));
            }
        }
        return books;
    }

    // Get all books with a certain number of pages
    public List<Book> getBooksByNumPages(int numPages, boolean greaterOrEqual) {
        List<Book> books = new ArrayList<>();
        if (greaterOrEqual) {
            for (Document doc : collection.find(Filters.gte("num_pages", numPages))) {
                books.add(new Book(doc));
            }
        } else {
            for (Document doc : collection.find(Filters.lt("num_pages", numPages))) {
                books.add(new Book(doc));
            }
        }
        return books;
    }


    // Get books by author ID using the author id inside books
    public List<Book> getBooksByAuthor(ObjectId authorId) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("authors.id", authorId))) {
            books.add(new Book(doc));
        }
        return books;
    }
    
    // Get books by multiple genres with AND/OR logic
    public List<Book> getBooksByGenres(List<String> genres, boolean isAnd) {
        List<Book> books = new ArrayList<>();
        if (isAnd) {
            for (Document doc : collection.find(Filters.all("genre", genres))) {
                books.add(new Book(doc));
            }
        } else {
            for (Document doc : collection.find(Filters.in("genre", genres))) {
                books.add(new Book(doc));
            }
        }
        return books;
    }

    // Update the rating of a book
    public boolean updateBookRating(ObjectId bookId, int rating, String nationality) {
        try {
            Document book = collection.find(Filters.eq("_id", bookId)).first();
            if (book != null) {
                int sumStars = book.getInteger("sumStars", 0) + rating;
                int numRatings = book.getInteger("numRatings", 0);
                numRatings = rating > 0 ? numRatings + 1 : numRatings - 1;

                // Aggiornamento del sumRating e della cardinality in base alla nazionalità
                Document ratingsAggByNat = book.get("ratings_agg_by_nat", Document.class);
                if (ratingsAggByNat == null) {
                    ratingsAggByNat = new Document();
                }
                Document nationalityStats = ratingsAggByNat.get(nationality, Document.class);

                // Se non esiste la statistica per la nazionalità, la inizializzo
                if (nationalityStats == null) {
                    nationalityStats = new Document("sumRating", 0).append("cardinality", 0);
                }

                int sumRatingByNat = nationalityStats.getInteger("sumRating", 0) + rating;
                int cardinality = nationalityStats.getInteger("cardinality", 0);
                cardinality = rating > 0 ? cardinality + 1 : cardinality - 1;

                collection.updateOne(
                    Filters.eq("_id", bookId),
                    Updates.combine(
                        Updates.set("sumStars", sumStars),
                        Updates.set("numRatings", numRatings),
                        Updates.set("ratings_agg_by_nat." + nationality + ".sumRating", sumRatingByNat),
                        Updates.set("ratings_agg_by_nat." + nationality + ".cardinality", cardinality)
                    )
                );
                return true;
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento dei rating del libro: " + e.getMessage());
        }
        return false;
    }

    // Methods to update the most useful reviews of a book according to the book document structure
    public void addMostUsefulReview(ObjectId bookId, Document review) {
        try {
            collection.updateOne(
                Filters.eq("_id", bookId), 
                Updates.push("most_10_useful_reviews", review)
            );
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento delle recensioni più utili del libro: " + e.getMessage());
        }
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find()) {
            books.add(new Book(doc));
        }
        return books;
    }
}


