package com.polarbookshop.catalog_service;

import com.polarbookshop.catalog_service.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CatalogServiceApplicationTests {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void contextLoads() {}

  @Test
  void whenPostRequestThenBookCreated() {
    var expectedBook = new Book("1234567890", "Title", "Author", 9.90);
    webTestClient
      .post()
      .uri("/books")
      .bodyValue(expectedBook)
      .exchange()
      .expectStatus()
      .isCreated()
      .expectBody(Book.class)
      .isEqualTo(expectedBook);
  }
}
