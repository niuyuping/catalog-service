package com.polarbookshop.catalog_service.domain;

import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import com.polarbookshop.catalog_service.infrastructure.persistence.DataConfig;
import reactor.test.StepVerifier;
import reactor.core.publisher.Mono;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@DataR2dbcTest
@Import(DataConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@Testcontainers
public class BookRepositoryR2dbcTests {

    @Container
    static PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>("postgres:17.4");

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                String.format("r2dbc:postgresql://%s:%d/%s",
                        postgresql.getHost(),
                        postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                        postgresql.getDatabaseName()));
        registry.add("spring.flyway.url", postgresql::getJdbcUrl);
        registry.add("spring.r2dbc.username", postgresql::getUsername);
        registry.add("spring.flyway.user", postgresql::getUsername);
        registry.add("spring.r2dbc.password", postgresql::getPassword);
        registry.add("spring.flyway.password", postgresql::getPassword);
    }

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    @Test
    void findBookByIsbnWhenExisting() {
        var bookIsbn = "1234567890";
        var bookToInsert = Book.of(bookIsbn, "Title", "Author", 12.90, "Publisher");

        Mono<Book> setupAndFindOperation = r2dbcEntityTemplate.insert(bookToInsert)
                .then(bookRepository.findByIsbn(bookIsbn));

        StepVerifier.create(setupAndFindOperation)
                .expectNextMatches(foundBook -> {
                    assertThat(foundBook).isNotNull();
                    assertThat(foundBook.isbn()).isEqualTo(bookToInsert.isbn());
                    return true;
                })
                .verifyComplete();
    }
}
