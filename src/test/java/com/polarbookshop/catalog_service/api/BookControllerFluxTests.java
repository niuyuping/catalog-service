package com.polarbookshop.catalog_service.api;

import com.polarbookshop.catalog_service.application.BookService;
import com.polarbookshop.catalog_service.domain.Book;
import com.polarbookshop.catalog_service.domain.BookNotFoundException;
import com.polarbookshop.catalog_service.domain.BookAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.mockito.BDDMockito.given;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

/**
 * BookController 的 WebFlux 切片测试类。
 * 仅加载 WebFlux 层组件和指定的 Controller，Service 层将被模拟。
 */
@WebFluxTest(BookController.class) // 1. 指定要测试的 Controller
class BookControllerFluxTests {

    @Autowired
    private WebTestClient webTestClient; // 2. 注入由 Spring Boot 自动配置的 WebTestClient，用于执行 HTTP 请求

    @MockitoBean // 使用 Spring Framework 提供的注解来模拟 Bean
    private BookService bookService; // 3. 创建 BookService 的模拟对象，隔离 Controller 依赖

    // --- 测试数据 ---
    private final String existingIsbn = "1234567891";
    private final String nonExistentIsbn = "9876543210";
    private final List<Book> expectedBooks = List.of(new Book(existingIsbn, "Title", "Author", 9.90),
            new Book("1234567892", "Title2", "Author2", 19.90));

    /**
     * 测试场景：当请求所有书籍时，应返回书籍列表。
     */
    @Test
    void whenGetAllBooksThenReturnBookList() throws Exception {

        given(bookService.viewBookList()).willReturn(Flux.fromIterable(expectedBooks));

        webTestClient.get().uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<Book>>() {
                })
                .value(actualBooks -> assertThat(actualBooks).isEqualTo(expectedBooks));
    }

    /**
     * 测试场景：当根据已存在的 ISBN 请求书籍时，应返回对应的书籍信息。
     */
    @Test
    void whenGetBookExistingThenReturnBook() throws Exception {

        given(bookService.viewBookDetails(existingIsbn)).willReturn(Mono.just(expectedBooks.get(0)));

        webTestClient.get().uri("/books/" + existingIsbn) // Execute GET request
                .exchange() // Perform the request and get the response
                .expectStatus().isOk() // Expect HTTP 200 OK
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isEqualTo(expectedBooks.get(0));
                });
    }

    /**
     * 测试场景：当根据不存在的 ISBN 请求书籍时，应返回 404 Not Found。
     */
    @Test
    void whenGetBookNotExistingThenReturnNotFound() throws Exception {

        given(bookService.viewBookDetails(nonExistentIsbn)).willThrow(new BookNotFoundException(nonExistentIsbn));

        webTestClient.get().uri("/books/" + nonExistentIsbn) // 执行 GET 请求
                .exchange() // Perform the request and get the response
                .expectStatus().isNotFound(); // Expect HTTP 404 Not Found
    }

    /**
     * 测试场景：当 POST 请求创建一本新书时，应返回 201 Created 和创建的书籍信息。
     */
    @Test
    void whenPostBookThenReturnCreated() throws Exception {
        var bookToCreate = expectedBooks.get(0);
        given(bookService.addBookToCatalog(bookToCreate)).willReturn(Mono.just(bookToCreate));

        webTestClient.post().uri("/books").bodyValue(bookToCreate).exchange().expectStatus().isCreated()
                .expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isEqualTo(bookToCreate);
                });
    }

    /**
     * 测试场景：当 POST 请求创建一本已存在的书籍时（根据 ISBN 判断），应返回 422 Unprocessable Entity。
     */
    @Test
    void whenPostExistingBookThenReturnUnprocessableEntity() throws Exception {
        var bookToCreate = expectedBooks.get(0);
        given(bookService.addBookToCatalog(bookToCreate))
                .willThrow(new BookAlreadyExistsException(bookToCreate.isbn()));

        webTestClient.post().uri("/books").bodyValue(bookToCreate).exchange().expectStatus()
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * 测试场景：当 POST 请求的书籍数据包含无效字段（例如无效的 ISBN 格式）时，应返回 400 Bad Request。
     * 这个测试验证的是 Spring 的 Bean Validation 集成。
     */
    @Test
    void whenPostBookWithInvalidIsbnThenReturnBadRequest() throws Exception {
        var bookToCreate = new Book("invalidIsbn", "Title", "Author", 9.90);
        // 注意：此处无需 mock bookService，因为验证发生在 Service 调用之前
        webTestClient.post().uri("/books").bodyValue(bookToCreate).exchange()
                .expectStatus().isBadRequest()
                // Use ParameterizedTypeReference for Map<String, String> (assuming error
                // format)
                .expectBody(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .value(errorResponse -> {
                    assertThat(errorResponse).containsKey("isbn");
                    // Assuming the validation message for @Pattern is "The ISBN format must be
                    // valid (10 or 13 digits)."
                    // You might need to adjust the expected message based on your actual Book
                    // validation annotation
                    assertThat(errorResponse.get("isbn")).isEqualTo("The ISBN format must be valid (10 or 13 digits).");
                    // The previous "格式错误" might be incorrect, adjust if necessary
                });
    }

    /**
     * 测试场景：当删除一本已存在的书籍时，应返回 204 No Content。
     */
    @Test
    void whenDeleteBookExistingThenReturnNoContent() throws Exception {
        var bookToDelete = expectedBooks.get(0);
        given(bookService.removeBookFromCatalog(bookToDelete.isbn())).willReturn(Mono.empty());

        webTestClient.delete().uri("/books/" + bookToDelete.isbn()) // 执行 DELETE 请求
                .exchange() // 执行请求并获取响应
                .expectStatus().isNoContent(); // 期望 HTTP 204 No Content
    }

    /**
     * 测试场景：当尝试删除一本不存在的书籍时，应返回 404 Not Found。
     */
    @Test
    void whenDeleteBookNotExistingThenReturnNotFound() throws Exception {
        given(bookService.removeBookFromCatalog(nonExistentIsbn)).willThrow(new BookNotFoundException(nonExistentIsbn));

        webTestClient.delete().uri("/books/" + nonExistentIsbn) // 执行 DELETE 请求
                .exchange() // 执行请求并获取响应
                .expectStatus().isNotFound(); // 期望 HTTP 404 Not Found
    }

    /**
     * 测试场景：当 PUT 请求更新一本已存在的书籍时，应返回 200 OK 和更新后的书籍信息。
     */
    @Test
    void whenPutBookExistingThenReturnUpdatedBook() throws Exception {
        var bookToUpdate = expectedBooks.get(0);
        given(bookService.editBookDetails(bookToUpdate.isbn(), bookToUpdate)).willReturn(Mono.just(bookToUpdate));

        webTestClient.put().uri("/books/" + bookToUpdate.isbn()).bodyValue(bookToUpdate).exchange().expectStatus()
                .isOk().expectBody(Book.class).value(actualBook -> {
                    assertThat(actualBook).isEqualTo(bookToUpdate);
                });
    }

    /**
     * 测试场景：当 PUT 请求更新一本不存在的书籍时，应返回 404 Not Found。
     */
    @Test
    void whenPutBookNotExistingThenReturnNotFound() throws Exception {
        var bookToUpdate = expectedBooks.get(0);
        given(bookService.editBookDetails(bookToUpdate.isbn(), bookToUpdate))
                .willThrow(new BookNotFoundException(bookToUpdate.isbn()));

        webTestClient.put().uri("/books/" + bookToUpdate.isbn()).bodyValue(bookToUpdate).exchange().expectStatus()
                .isNotFound();
    }
}