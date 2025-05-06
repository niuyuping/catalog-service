package com.polarbookshop.catalog_service.infrastructure.persistence;

import com.polarbookshop.catalog_service.domain.Book;
import com.polarbookshop.catalog_service.domain.BookRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class ImMemoryBookRepository implements BookRepository {

  private final Map<String, Book> books = new ConcurrentHashMap<>();

  @Override
  public Flux<Book> findAll() {
    return Flux.fromIterable(books.values());
  }

  @Override
  public Mono<Book> findByIsbn(String isbn) {
    return Mono.justOrEmpty(books.get(isbn));
  }

  @Override
  public Mono<Book> save(Book book) {
    books.put(book.isbn(), book);
    return Mono.just(book);
  }

  @Override
  public Mono<Void> deleteByIsbn(String isbn) {
    books.remove(isbn);
    return Mono.empty();
  }

  @Override
  public Mono<Boolean> existsByIsbn(String isbn) {
    return Mono.just(books.containsKey(isbn));
  }
}
