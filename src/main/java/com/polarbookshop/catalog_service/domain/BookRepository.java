package com.polarbookshop.catalog_service.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookRepository {
  Flux<Book> findAll();
  Mono<Book> findByIsbn(String isbn);
  Mono<Boolean> existsByIsbn(String isbn);
  Mono<Book> save(Book book);
  Mono<Void> deleteByIsbn(String isbn);
}
