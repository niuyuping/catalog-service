package com.polarbookshop.catalog_service.api;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestBody;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseBody;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(HomeController.class)
@AutoConfigureRestDocs
class HomeControllerTests {

  @Autowired
  private WebTestClient webClient;

  @Test
  void whenGetRootThenReturnWelcomeMessage() {
    webClient
      .get()
      .uri("/")
      .exchange()
      .expectStatus()
      .isOk()
      .expectBody(String.class)
      .isEqualTo("Welcome to the book catalog!")
      .consumeWith(
        document(
          "get-greeting",
          preprocessRequest(prettyPrint()),
          preprocessResponse(prettyPrint()),
          requestBody(),
          responseBody()
        )
      );
  }
}
