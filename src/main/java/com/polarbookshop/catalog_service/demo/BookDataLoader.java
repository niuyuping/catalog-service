package com.polarbookshop.catalog_service.demo;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import reactor.core.publisher.Flux;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.polarbookshop.catalog_service.domain.Book;
import com.polarbookshop.catalog_service.domain.BookRepository;

@Component
@ConditionalOnProperty(
    name = "polar.testdata.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class BookDataLoader {

    private static final Logger log = LoggerFactory.getLogger(BookDataLoader.class);
    private final BookRepository bookRepository;

    public BookDataLoader(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadBookTestData() {
        log.info("Test data loading is enabled. Starting...");

        var book1 = Book.of("1234567891", "Northern Lights", "Lyra Silverstar", 9.90);
        var book2 = Book.of("1234567892", "Polar Journey", "Iorek Polarson", 12.90);

        bookRepository.deleteAll()
            .thenMany(
                Flux.concat(
                        bookRepository.save(book1),
                        bookRepository.save(book2)
                    )
                    .doOnNext(book -> log.info("Book saved: {}", book.isbn()))
            )
            .subscribe(
                null,
                error -> log.error("Error loading test data", error),
                () -> log.info("Finished loading test data.")
            );
        
        log.info("BookDataLoader method finished executing (data loading may still be in progress asynchronously).");
    }
}
