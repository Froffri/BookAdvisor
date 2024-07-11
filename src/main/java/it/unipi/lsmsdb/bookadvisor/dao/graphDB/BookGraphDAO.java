package it.unipi.lsmsdb.bookadvisor.dao.graphDB;

import static org.neo4j.driver.Values.parameters;

import org.bson.types.ObjectId;
import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;

import it.unipi.lsmsdb.bookadvisor.model.book.Book;


public class BookGraphDAO {
    private final Neo4jConnector connector;

    public BookGraphDAO(Neo4jConnector connector) {
        this.connector = connector;
    }

    // CREATE

    /**
     * Create a book node in the graph database
     * @param book
     */
    public boolean addBook(Book book) {
        try (Session session = connector.getSession()) {
            return session.run(
                "MERGE (b:Book {id: $id}) " + 
                "ON CREATE SET b.title = $title, b.language = $language " +
                "RETURN b", 
                parameters("id", book.getId().toHexString(), 
                            "title", book.getTitle(), 
                            "language", book.getLanguage())
            ).consume().counters().nodesCreated() > 0;

            // if (result.hasNext())
            //     return true;
            // else 
            //     return false;
        } catch (Neo4jException e) {
            return false;
        }
    }

    // READ

    public Book getBookById(ObjectId bookId) {
        try (Session session = connector.getSession()) {

            // Convert ObjectId to string
            String idString = bookId.toHexString();
            Result result = session.run(
                "MATCH (b:Book {id: $id}) " +
                "RETURN b", 
                parameters("id", idString)
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
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String idString = book.getId().toHexString();
            return session.run(
                "MATCH (b:Book {id: $id}) " +
                "SET b.title = $title, b.language = $language " +
                "RETURN b", 
                parameters("id", idString,
                            "title", book.getTitle(),
                            "language", book.getLanguage())
            ).consume().counters().containsUpdates();

            // if (result.hasNext())
            //     return true;
            // else 
            //     return false;
            
        } catch (Neo4jException e) {
            return false;
        }
    }

    // DELETE

    /**
     * Delete a book node in the graph database
     * @param book
     */
    public boolean deleteBook(Book book) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String idString = book.getId().toHexString();
            return session.run(
                "MATCH (b:Book {id: $id})" +
                "DETACH DELETE b",
                parameters("id", idString)
            ).consume().counters().nodesDeleted() > 0;

            // return result.hasNext();
        } catch(Neo4jException e) {
            return false;
        }
    }

    /**
     * Delete a book node in the graph database
     * @param bookId
     */
    public boolean deleteBookById(ObjectId bookId) {
        try (Session session = connector.getSession()) {
            // Convert ObjectId to string
            String idString = bookId.toHexString();
            return session.run(
                "MATCH (b:Book {id: $id})" +
                "DETACH DELETE b",
                parameters("id", idString)
            ).consume().counters().nodesDeleted() > 0;

        } catch (Neo4jException e) {
            return false;
        }
    }

}
