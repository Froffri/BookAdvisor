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
    public boolean addBook(Book book) {
        try (Session session = driver.session()) {
            session.run(
                "MERGE (b:Book {id: $id}) " + 
                "ON CREATE SET b.title = $title, b.language = '$language'", 
                parameters("id", book.getId(), 
                            "title", book.getTitle(), 
                            "language", book.getLanguage())
            );
        } catch (Neo4jException e) {
            return false;
        }
        return true;
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
     * @param book
     */
    public boolean updateBook(Book book) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (b:Book {id: $id})" +
                "SET b.title = $title, b.language = '$language'",
                parameters("id", book.getId(),
                                    "title", book.getTitle(),
                                    "language", book.getLanguage())
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
    public boolean deleteBook(Book book) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (b:Book {id: $id}) DELETE b",
                parameters("id", book.getId())
            );
        } catch(Neo4jException e) {
            return false;
        }
        return true;
    }

    /**
     * Delete a book node in the graph database
     * @param bookId
     */
    public boolean deleteBookById(ObjectId bookId) {
        try (Session session = driver.session()) {
            session.run(
                "MATCH (b:Book {id: $id}) DELETE b",
                parameters("id", bookId)
            );
        } catch (Neo4jException e) {
            return false;
        }
        return true;
    }

}
