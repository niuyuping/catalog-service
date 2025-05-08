package com.polarbookshop.catalog_service.api;

import com.polarbookshop.catalog_service.application.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import com.polarbookshop.catalog_service.domain.BookNotFoundException;

@WebFluxTest(BookController.class)
public class BookControllerTests {

    @Autowired
    private WebTestClient webTestClient;
    
    @MockitoBean
    private BookService bookService;

    @Test
    void whenGetBookNotExistingThenReturn404() {
        var bookIsbn = "1234567890";
        Mockito.when(bookService.viewBookDetails(bookIsbn))
            .thenReturn(Mono.error(new BookNotFoundException(bookIsbn)));
        webTestClient
            .get()
            .uri("/books/" + bookIsbn)
            .exchange()
            .expectStatus().isNotFound();
    }

}
