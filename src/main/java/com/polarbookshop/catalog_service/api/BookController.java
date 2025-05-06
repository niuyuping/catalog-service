package com.polarbookshop.catalog_service.api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import com.polarbookshop.catalog_service.application.BookService;
import com.polarbookshop.catalog_service.domain.Book;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @GetMapping
  public Flux<Book> bookList() {
    return bookService.viewBookList();
  }
  
  @GetMapping("/{isbn}")
  public Mono<Book> bookDetails(@PathVariable String isbn) {
    return bookService.viewBookDetails(isbn);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)   
  public Mono<Book> addBookToCatalog(@Valid @RequestBody Book book) {
    return bookService.addBookToCatalog(book);
  }

  @DeleteMapping("/{isbn}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Mono<Void> deleteBookFromCatalog(@PathVariable String isbn) {
    return bookService.removeBookFromCatalog(isbn);
  }

  @PutMapping("/{isbn}")
  @ResponseStatus(HttpStatus.OK)
  public Mono<Book> updateBook(@PathVariable String isbn, @Valid @RequestBody Book book) {
    return bookService.editBookDetails(isbn, book);
  }
}

