package it.unipi.lsmsdb.bookadvisor.dao.graphDB;

import static org.neo4j.driver.Values.parameters;

import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;

import it.unipi.lsmsdb.bookadvisor.model.book.Book;


public class BookGraphDAO {
    private final Driver driver;

    public BookGraphDAO(String uri, String username, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public BookGraphDAO(Driver driver) {
        this.driver = driver;
    }

    public void close() {
        this.driver.close();
    }

    // CREATE

    /**
     * Create a book node in the graph database
     * @param book
     */
    public void addBook(Book book) {
        try (Session session = driver.session()) {
            session.run(
                "MERGE (b:Book {id: $id}) " + 
                "ON CREATE SET b.sum_stars = $sumStars, b.num_ratings = $numRatings, b.language = '$language', b.genre = '$genre'", 
                parameters("id", book.getId(), 
                            "sumStars", book.getSumStars(), 
                            "numRatings", book.getNumRatings(), 
                            "language", book.getLanguage(), 
                            "genre", book.getGenre())
            );
        }
    }

    // READ

    public Book getBookById(ObjectId bookId) {
        try (Session session = driver.session()) {
            Result result = session.run(
                "MATCH (b:Book {id: $id}) " +
                "RETURN b", 
                parameters("id", bookId)
            );

            if (result.hasNext()) {
                Node bookNode = result.next().get("b").asNode();
            
                return new Book(bookNode);
            }
            return null;
        }
        
    }

    // UPDATE
    /**
     * Update a book node in the graph database
     * @param bookId
     * @param book
     */
    public boolean updateBook(ObjectId bookId, Book book) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (b:Book {id: $id})" +
                "SET b.sum_stars = $sumStars, b.num_ratings = $numRatings, b.language = '$language', b.genre = '$genre'",
                parameters("id", bookId,
                                    "sumStars", book.getSumStars(),
                                    "numRatings", book.getNumRatings(),
                                    "language", book.getLanguage(),
                                    "genre", book.getGenre())
                );
        } catch (Neo4jException e) {
            return false;
        }
        return true;
    }

    // DELETE

    /**
     * Delete a book node in the graph database
     * @param book
     */
    public void deleteBook(Book book) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (b:Book {id: $id}) DELETE b",
                parameters("id", book.getId())
            );
        }
    }

    /**
     * Delete a book node in the graph database
     * @param bookId
     */
    public void deleteBookById(ObjectId bookId) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (b:Book {id: $id}) DELETE b",
                parameters("id", bookId)
            );
        }
    }

}
