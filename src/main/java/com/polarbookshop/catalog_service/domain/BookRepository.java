package com.polarbookshop.catalog_service.domain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Define the reactive contract for any Book Repository implementation
public interface BookRepository {

    Flux<Book> findAll(); // Return Flux<Book>

    Mono<Book> findByIsbn(String isbn); // Return Mono<Book>

    Mono<Boolean> existsByIsbn(String isbn); // Return Mono<Boolean>

    Mono<Book> save(Book book); // Return Mono<Book>

    Mono<Void> deleteByIsbn(String isbn); // Return Mono<Void>

    Mono<Void> deleteAll(); // Return Mono<Void>
} 