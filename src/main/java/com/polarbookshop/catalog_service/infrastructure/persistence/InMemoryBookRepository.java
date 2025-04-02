package com.polarbookshop.catalog_service.infrastructure.persistence;

import com.polarbookshop.catalog_service.domain.Book;
import com.polarbookshop.catalog_service.domain.BookRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBookRepository implements BookRepository {

    private static final Map<String, Book> books = new ConcurrentHashMap<>();

    @Override
    public Flux<Book> findAll() {
        return Flux.fromIterable(books.values());
    }

    @Override
    public Mono<Book> findByIsbn(String isbn) {
        return Mono.justOrEmpty(books.get(isbn));
    }

    @Override
    public Mono<Boolean> existsByIsbn(String isbn) {
        return Mono.just(books.containsKey(isbn));
    }

    @Override
    public Mono<Book> save(Book book) {
        return Mono.fromSupplier(() -> {
            books.put(book.isbn(), book);
            return book;
        });
    }

    @Override
    public Mono<Void> deleteByIsbn(String isbn) {
        return Mono.fromRunnable(() -> books.remove(isbn))
                   .then();
    }

    @Override
    public Mono<Void> deleteAll() {
        return Mono.fromRunnable(() -> books.clear())
                   .then();
    }
} 