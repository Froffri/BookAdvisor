package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.BookDao;
import it.unipi.lsmsdb.bookadvisor.dao.documentDB.UserDao;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.BookGraphDAO;
import it.unipi.lsmsdb.bookadvisor.model.book.Book;
import it.unipi.lsmsdb.bookadvisor.model.user.*;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BookService {
    private BookDao bookDao;
    private UserDao userDao;
    private BookGraphDAO bookGraphDAO;

    public BookService(BookDao bookDao, UserDao userDao, BookGraphDAO bookGraphDAO) {
        this.bookDao = bookDao;
        this.userDao = userDao;
        this.bookGraphDAO = bookGraphDAO;
    }

    public Book findBookById(ObjectId id) {
        return bookDao.findBookById(id);
    }

    public List<Book> findBooksByTitle(String title) {
        return bookDao.findBooksByTitle(title);
    }

    public boolean addBook(Book book, Reviewer user) {
        System.out.println("Adding book: " + book.toString());
        if (user instanceof Author) {
            if(bookDao.addBook(book)){
                // Successfully added the book in mongodb
                if(bookGraphDAO.addBook(book)){
                    // Successfully added the book in neo4j
                    return true;
                } else {
                    // Failed to add the book in neo4j
                    System.out.println("Failed to insert book in graph");
                    List<ObjectId> authorIds = new ArrayList<>();
                    for (Book.Author author : book.getAuthors()) {
                        authorIds.add(author.getId());
                    }
                    bookDao.deleteBook(book.getId());
                    return false;
                }
            }
            // Failed to add the book in mongodb
            return false;

        } else {
            System.out.println("Solo gli autori possono inserire libri.");
            return false;
        }
    }

    public boolean updateBook(Book book, Reviewer user) {
        if (user instanceof Admin) {
            // Get the old book from the database
            Book oldBook = bookDao.getBookById(book.getId());

            if(bookDao.updateBook(book.getId(), book.toDocument())){
                // Successfully updated the book in mongodb
                if(bookGraphDAO.updateBook(book)){
                    // Successfully updated the book in neo4j
                    return true;
                } else {
                    // Failed to update the book in neo4j
                    System.out.println("Failed to update book in graph");
                    bookDao.updateBook(oldBook.getId(), oldBook.toDocument());
                    return false;
                }
            }
            return false;
        } else if (user instanceof Author) {
            Author author = (Author) user;
            boolean authorExists = Arrays.stream(book.getAuthors())
                .anyMatch(a -> a.getId().equals(author.getId()));

            if (authorExists) {

                // Get the old book from the database
                Book oldBook = bookDao.getBookById(book.getId());

                if(bookDao.updateBook(book.getId(), book.toDocument())){

                    // Successfully updated the book in mongodb
                    if(bookGraphDAO.updateBook(book)){
                        // Successfully updated the book in neo4j
                        return true;
                    } else {
                        // Failed to update the book in neo4j
                        System.out.println("Failed to update book in graph");
                        bookDao.updateBook(oldBook.getId(), oldBook.toDocument());
                        return false;
                    }
                }
                return false;

            } else {
                System.out.println("Gli autori possono modificare solo libri di cui sono autori.");
                return false;
            }
        
        } else {
            System.out.println("Solo gli amministratori o gli autori possono modificare i libri.");
            return false;
        }
    }

    public boolean deleteBook(Book book, Reviewer user) {
        if (user instanceof Admin) {

            if (bookDao.deleteBook(book.getId())) {
                // Successfully deleted the book in mongodb
                if (bookGraphDAO.deleteBook(book)) {
                    // Successfully deleted the book in neo4j
                    return true;
                } else {
                    // Failed to delete the book in neo4j
                    System.out.println("Failed to delete book in graph");
                    bookDao.addBook(book.getId(), book);
                    return false;
                }
            } else {
                // Failed to delete the book in mongodb
                System.out.println("Failed to delete book in mongodb");
                return false;
            }
        } else if (user instanceof Author) {
            Author author = (Author) user;
            boolean authorExists = Arrays.stream(book.getAuthors())
                .anyMatch(a -> a.getId().equals(author.getId()));

            if (authorExists) {

                if (bookDao.deleteBook(book.getId())) {
                    // Successfully deleted the book in mongodb
                    if (bookGraphDAO.deleteBook(book)) {
                        // Successfully deleted the book in neo4j
                        return true;
                    } else {
                        // Failed to delete the book in neo4j
                        System.out.println("Failed to delete book in graph");
                        bookDao.addBook(book.getId(), book);
                        return false;
                    }
                } else {
                    // Failed to delete the book in mongodb
                    System.out.println("Failed to delete book in mongodb");
                    return false;
                }
            } else {
                System.out.println("Gli autori possono cancellare solo libri di cui sono autori.");
                return false;
            }
        
        } else {
            System.out.println("Solo gli amministratori o gli autori possono cancellare i libri.");
            return false;
        }
    }

    public List<Book> getAllBooks() {
        return bookDao.getAllBooks();
    }

    public Book getBookById(ObjectId id) {
        return bookDao.getBookById(id);
    }
    
    public List<Book> getBooksByTitle(String title) {
        return bookDao.getBooksByTitle(title);
    }

    public List<Book> getBooksByAuthor(ObjectId authorId) {
        return bookDao.getBooksByAuthor(authorId);
    }

    public List<Book> getBooksByGenre(String genre) {
        return bookDao.getBooksByGenre(genre);
    }

    public List<Book> getBooksByYear(int year) {
        return bookDao.getBooksByYear(year);
    }

    public List<Book> getBooksByLanguage(String language) {
        return bookDao.getBooksByLanguage(language);
    }

    public List<Book> getBooksByRating(double targetRating, boolean greaterOrEqual) {
        return bookDao.getBooksByRating(targetRating, greaterOrEqual);
    }

    public List<Book> getBooksByNumPages(int numPages, boolean greaterOrEqual) {
        return bookDao.getBooksByNumPages(numPages, greaterOrEqual);
    }

    public List<Book> getBooksByGenres(List<String> genres, boolean isAnd){
        return bookDao.getBooksByGenres(genres, isAnd);
    }

    // Method to get top N popular books based on numRatings
    public List<Book> getPopularBooks(int limit) {        
        List<Book> allBooks = bookDao.getAllBooks();
        
        return allBooks.stream()                
                .sorted((b1, b2) -> Integer.compare(b2.getNumRatings(), b1.getNumRatings()))
                .limit(limit)                
                .collect(Collectors.toList());
    }
}


