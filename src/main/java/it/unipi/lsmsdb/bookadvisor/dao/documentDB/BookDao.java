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


// The structure of the mongoDB document for the book is as follows:
// {
//     "_id": ObjectId("623a45b7123b450a432b1c45"),
//     "sumStars": 1250,
//     "numRatings": 300,
//     "language": "English",
//     "title": "The Great Adventure",
//     "author": [ObjectId("623a45b7123b450a432b1a34")],
//     "genre": ["Adventure", "Mystery"],
//     "year": 2021,
//     "imageUrl": "http://example.com/images/the-great-adventure.jpg",
//     "numPages": 320,
//     "ratingsAggByNat": {
//       "US": {
//         "sumRating": 820,
//         "cardinality": 150
//       },
//       "UK": {
//         "sumRating": 430,
//         "cardinality": 100
//       }
//     },
//     "most10UsefulReviews": [
//       {
//         "reviewId": ObjectId("623a45b7123b450a432b1f56"),
//         "userId": ObjectId("623a45b7123b450a432b1b23"),
//         "userName": "JohnDoe92",
//         "text": "A thrilling journey through uncharted lands!",
//         "stars": 5,
//         "date": ISODate("2022-03-15T14:00:00Z"),
//         "helpfulVotes": 112,
//         "language": "en"
//       }
//       {
//         "reviewId": ObjectId("623a45b7123b450a432b1f57"),
//         "userId": ObjectId("623a45b7123b450a432b1b24"),
//         "userName": "Leia93",
//         "text": "An unexpected turns of events",
//         "stars": 4,
//         "date": ISODate("2022-01-15T14:00:00Z"),
//         "helpfulVotes": 93,
//         "language": "en"
//       }
//     ]
//   }


public class BookDao {
    private static final String COLLECTION_NAME = "books";
    private MongoCollection<Document> collection;

    // Constructor
    public BookDao(MongoDBConnector connector) {
        MongoDatabase database = connector.getDatabase();
        collection = database.getCollection(COLLECTION_NAME);
    }

    // Find a book by its ID
    public Optional<Book> findBookById(ObjectId id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
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
    public void addBook(Book book) {
        try {
            Document doc = book.toDocument();
            collection.insertOne(doc);
        } catch (Exception e) {
            System.err.println("Errore durante l'inserimento del libro: " + e.getMessage());
        }
    }

    // Update a book's information
    public void updateBook(Book book) {
        try {
            collection.updateOne(Filters.eq("_id", book.getId()),
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
    public void deleteBook(ObjectId id) {
        try {
            collection.deleteOne(Filters.eq("_id", id));
        } catch (Exception e) {
            System.err.println("Errore durante la cancellazione del libro: " + e.getMessage());
        }
    }

    // Get all books by th title
    public List<Book> getBooksByTitle(String title) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("title", title))) {
            books.add(new Book(doc));
        }
        return books;
    }

    // Get all books by a given author
    public List<Book> getBooksByAuthor(ObjectId author) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find(Filters.in("author", author))) {
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
        for (Document doc : collection.find(Filters.in("year", year))) {
            books.add(new Book(doc));
        }
        return books;
    }

    // Get all books of a given language
    public List<Book> getBooksByLanguage(String language) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find(Filters.in("language", language))) {
            books.add(new Book(doc));
        }
        return books;
    }

    // Get all books with a certain rating
    public List<Book> getBooksByRating(double targetRating, boolean greaterOrEqual) {
        List<Book> books = new ArrayList<>();
        for (Document doc : collection.find()) {
            int sumStars = doc.getInteger("sumstars", 0); // 0 is a default value if sumstars is not present
            int numRatings = doc.getInteger("numratings", 0); // 0 is a default value if numratings is not present
    
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
            for (Document doc : collection.find(Filters.gte("numPages", numPages))) {
                books.add(new Book(doc));
            }
        } else {
            for (Document doc : collection.find(Filters.lt("numPages", numPages))) {
                books.add(new Book(doc));
            }
        }
        return books;
    }

    // Get books by multiple authors with AND/OR logic
    public List<Book> getBooksByAuthors(List<ObjectId> authors, boolean isAnd) {
        List<Book> books = new ArrayList<>();
        if (isAnd) {
            for (Document doc : collection.find(Filters.all("author", authors))) {
                books.add(new Book(doc));
            }
        } else {
            for (Document doc : collection.find(Filters.in("author", authors))) {
                books.add(new Book(doc));
            }
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
    protected void updateBookRating(ObjectId bookId, int rating, String nationality) {
        try {
            Document book = collection.find(Filters.eq("_id", bookId)).first();
            if (book != null) {
                int sumStars = book.getInteger("sumStars", 0) + rating;
                int numRatings = book.getInteger("numRatings", 0);
                numRatings = rating > 0 ? numRatings + 1 : numRatings - 1;

                // Aggiornamento del sumRating e della cardinality in base alla nazionalità
                Document ratingsAggByNat = book.get("ratingsAggByNat", Document.class);
                Document nationalityStats = ratingsAggByNat.get(nationality, Document.class);
                int sumRatingByNat = nationalityStats.getInteger("sumRating", 0) + rating;
                int cardinality = nationalityStats.getInteger("cardinality", 0);
                cardinality = rating > 0 ? cardinality + 1 : cardinality - 1;
                
                collection.updateOne(
                    Filters.eq("_id", bookId),
                    Updates.combine(
                        Updates.set("sumStars", sumStars),
                        Updates.set("numRatings", numRatings),
                        Updates.set("ratingsAggByNat." + nationality + ".sumRating", sumRatingByNat),
                        Updates.set("ratingsAggByNat." + nationality + ".cardinality", cardinality)
                    )
                );
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento dei rating del libro: " + e.getMessage());
        }
    }

    // Methods to update the most useful reviews of a book according to the book document structure
    public void addMostUsefulReview(ObjectId bookId, Document review) {
        try {
            collection.updateOne(Filters.eq
                ("_id", bookId), Updates.push("most10UsefulReviews", review));
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiornamento delle recensioni più utili del libro: " + e.getMessage());
        }
    }
   
}