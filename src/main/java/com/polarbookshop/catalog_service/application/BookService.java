package com.polarbookshop.catalog_service.application;

import com.polarbookshop.catalog_service.domain.Book;
import com.polarbookshop.catalog_service.domain.BookAlreadyExistsException;
import com.polarbookshop.catalog_service.domain.BookNotFoundException;
import com.polarbookshop.catalog_service.domain.BookRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Flux<Book> viewBookList() {
        return bookRepository.findAll();
    }

    public Mono<Book> viewBookDetails(String isbn) {
        return bookRepository.findByIsbn(isbn)
                .switchIfEmpty(Mono.error(new BookNotFoundException(isbn)));
    }

    public Mono<Book> addBookToCatalog(Book book) {
        return bookRepository.existsByIsbn(book.isbn())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BookAlreadyExistsException(book.isbn()));
                    } else {
                        return bookRepository.save(book);
                    }
                });
    }

    public Mono<Void> removeBookFromCatalog(String isbn) {
        return bookRepository.existsByIsbn(isbn)
                .flatMap(exists -> {
                    if (!exists) {
                         return Mono.error(new BookNotFoundException(isbn));
                    } else {
                        return bookRepository.deleteByIsbn(isbn);
                    }
                });
    }

    public Mono<Book> editBookDetails(String isbn, Book book) {
        return bookRepository.findByIsbn(isbn)
                .flatMap(existingBook -> {
                    var bookToUpdate = new Book(
                        existingBook.id(),
                        existingBook.isbn(),
                        book.title(),
                        book.author(),
                        book.price(),
                        book.publisher(),
                        existingBook.version(),
                        existingBook.createdDate(),
                        existingBook.lastModifiedDate()
                    );
                    return bookRepository.save(bookToUpdate);
                })
                .switchIfEmpty(addBookToCatalog(book));
    }

} 