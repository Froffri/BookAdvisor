package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.BookDao;
import it.unipi.lsmsdb.bookadvisor.dao.graphDB.BookGraphDAO;
import it.unipi.lsmsdb.bookadvisor.model.book.Book;
import it.unipi.lsmsdb.bookadvisor.model.user.*;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BookService {
    private BookDao bookDao;
    private BookGraphDAO bookGraphDAO;

    public BookService(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    public Optional<Book> findBookById(String id) {
        return bookDao.findBookById(new ObjectId(id));
    }

    public List<Book> findBooksByTitle(String title) {
        return bookDao.findBooksByTitle(title);
    }

    public boolean addBook(Book book, User user) {
        if (user instanceof Author) {
            if(bookDao.addBook(book)){
                // Successfully added the book in mongodb
                if(bookGraphDAO.addBook(book)){
                    // Successfully added the book in neo4j
                    return true;
                } else {
                    // Failed to add the book in neo4j
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

    public boolean updateBook(Book book, User user) {
        Book oldBook = bookDao.findBookById(book.getId()).get();

        if (user instanceof Author) {
            Author author = (Author) user;
            if (Arrays.asList(book.getAuthors()).contains(author.getId())) {
                if(bookDao.updateBook(book)){
                    // Successfully updated the book in mongodb
                    if(bookGraphDAO.updateBook(book)){
                        // Successfully updated the book in neo4j
                        return true;
                    } else {
                        // Failed to update the book in neo4j
                        bookDao.updateBook(oldBook);
                        return false;
                    }
                }
                return false;

            } else {
                System.out.println("Gli autori possono aggiornare solo libri di cui sono autori.");
                return false;
            }
        } else if (user instanceof Admin) {
            if(bookDao.updateBook(book)){
                // Successfully updated the book in mongodb
                if(bookGraphDAO.updateBook(book)){
                    // Successfully updated the book in neo4j
                    return true;
                } else {
                    // Failed to update the book in neo4j
                    bookDao.updateBook(oldBook);
                    return false;
                }
            }
            return false;
        } else {
            System.out.println("Solo gli amministratori o gli autori possono aggiornare i libri.");
            return false;
        }
    }

    public boolean deleteBook(Book book, User user) {
        if (user instanceof Admin) {
            if(bookDao.deleteBook(book.getId())){
                // Successfully deleted the book in mongodb
                if(bookGraphDAO.deleteBook(book)){
                    // Successfully deleted the book in neo4j
                    return true;
                } else {
                    // Failed to delete the book in neo4j
                    bookDao.addBook(book);
                    return false;
                }
            }
            return false;
        } else if (user instanceof Author) {
            Author author = (Author) user;
            Optional<Book> optionalBook = bookDao.findBookById(book.getId());
    
            if (optionalBook.isPresent() && Arrays.asList(optionalBook.get().getAuthors()).contains(author.getId())) {
                if(bookDao.deleteBook(book.getId())){
                    // Successfully deleted the book in mongodb
                    if(bookGraphDAO.deleteBook(book)){
                        // Successfully deleted the book in neo4j
                        return true;
                    } else {
                        // Failed to delete the book in neo4j
                        bookDao.addBook(book);
                        return false;
                    }
                }
                return false;
            } else {
                System.out.println("Solo gli amministratori o gli autori associati possono cancellare i libri.");
                return false;
            }
        } else {
            System.out.println("Solo gli amministratori o gli autori possono cancellare i libri.");
            return false;
        }
    }
    
    public List<Book> getBooksByTitle(String title) {
        return bookDao.getBooksByTitle(title);
    }

    public List<Book> getBooksByAuthor(String authorId) {
        return bookDao.getBooksByAuthor(new ObjectId(authorId));
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

    public List<Book> getBooksByAuthors(List<ObjectId> authors, boolean isAnd){
        return bookDao.getBooksByAuthors(authors, isAnd);
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


