package it.unipi.lsmsdb.bookadvisor.service;

import it.unipi.lsmsdb.bookadvisor.dao.documentDB.BookDao;
import it.unipi.lsmsdb.bookadvisor.model.book.Book;
import it.unipi.lsmsdb.bookadvisor.model.user.*;
import org.bson.types.ObjectId;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BookService {
    private BookDao bookDao;

    public BookService(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    public Optional<Book> findBookById(String id) {
        return bookDao.findBookById(new ObjectId(id));
    }

    public List<Book> findBooksByTitle(String title) {
        return bookDao.findBooksByTitle(title);
    }

    public void addBook(Book book, User user) {
        if (user instanceof Author) {
            bookDao.addBook(book);
        } else {
            throw new IllegalArgumentException("Solo gli autori possono inserire libri.");
        }
    }

    public void updateBook(Book book, User user) {
        if (user instanceof Author) {
            Author author = (Author) user;
            if (Arrays.asList(book.getAuthor()).contains(author.getId())) {
                bookDao.updateBook(book);
            } else {
                throw new IllegalArgumentException("Gli autori possono aggiornare solo libri di cui sono autori.");
            }
        } else if (user instanceof Admin) {
            bookDao.updateBook(book);
        } else {
            throw new IllegalArgumentException("Solo gli amministratori o gli autori possono aggiornare i libri.");
        }
    }

    public void deleteBook(String id, User user) {
        if (user instanceof Admin) {
            bookDao.deleteBook(new ObjectId(id));
        } else if (user instanceof Author) {
            Author author = (Author) user;
            Optional<Book> optionalBook = bookDao.findBookById(new ObjectId(id));
    
            if (optionalBook.isPresent() && Arrays.asList(optionalBook.get().getAuthor()).contains(author.getId())) {
                bookDao.deleteBook(new ObjectId(id));
            } else {
                throw new IllegalArgumentException("Solo gli amministratori o gli autori associati possono cancellare i libri.");
            }
        } else {
            throw new IllegalArgumentException("Solo gli amministratori o gli autori possono cancellare i libri.");
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
}
