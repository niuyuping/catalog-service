package com.polarbookshop.catalog_service.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

// Define the reactive contract for any Book Repository implementation
public interface BookRepository extends ReactiveCrudRepository<Book, Long> {

    // Flux<Book> findAll(); // Return Flux<Book>

    Mono<Book> findByIsbn(String isbn); // Return Mono<Book>

    Mono<Boolean> existsByIsbn(String isbn); // Return Mono<Boolean>

    // Mono<Book> save(Book book); // Return Mono<Book>

    @Modifying
    @Transactional
    @Query("DELETE FROM Book WHERE isbn = :isbn")
    Mono<Void> deleteByIsbn(String isbn); // Return Mono<Void>

    // Mono<Void> deleteAll(); // Return Mono<Void>
}