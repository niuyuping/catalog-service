package com.polarbookshop.catalog_service.api;

import com.polarbookshop.catalog_service.application.BookService;
import com.polarbookshop.catalog_service.domain.Book;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController // Identifies this class as a REST controller
@RequestMapping("books") // Maps requests starting with /books to this controller
public class BookController {

    private final BookService bookService;

    // Constructor injection for BookService
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping // Maps GET requests to /books
    public Flux<Book> get() {
        return bookService.viewBookList();
    }

    @GetMapping("{isbn}") // Maps GET requests to /books/{isbn}
    public Mono<Book> getByIsbn(@PathVariable String isbn) {
        return bookService.viewBookDetails(isbn);
    }

    @PostMapping // Maps POST requests to /books
    @ResponseStatus(HttpStatus.CREATED) // Sets the HTTP status to 201 Created on success
    public Mono<Book> post(@Valid @RequestBody Mono<Book> bookMono) {
        return bookMono.flatMap(bookService::addBookToCatalog);
    }

    @DeleteMapping("{isbn}") // Maps DELETE requests to /books/{isbn}
    @ResponseStatus(HttpStatus.NO_CONTENT) // Sets the HTTP status to 204 No Content on success
    public Mono<Void> delete(@PathVariable String isbn) {
        return bookService.removeBookFromCatalog(isbn);
    }

    @PutMapping("{isbn}") // Maps PUT requests to /books/{isbn}
    public Mono<Book> put(@PathVariable String isbn, @Valid @RequestBody Mono<Book> bookMono) {
        return bookMono.flatMap(book -> bookService.editBookDetails(isbn, book));
    }

} 