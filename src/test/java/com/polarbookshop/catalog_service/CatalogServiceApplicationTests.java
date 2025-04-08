package com.polarbookshop.catalog_service;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import com.polarbookshop.catalog_service.domain.Book;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.http.HttpStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.junit.jupiter.api.AfterAll;

/**
 * 集成测试类，用于测试 Catalog Service 的 API 端点。
 * 使用 @SpringBootTest 启动一个完整的 Spring 应用上下文，并在随机端口上监听。
 * 使用 @TestInstance(TestInstance.Lifecycle.PER_CLASS) 使得 @BeforeAll 和 @AfterAll
 * 方法可以是实例方法，方便访问注入的 WebTestClient 和其他实例字段。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CatalogServiceApplicationTests {

	/**
	 * 注入 WebTestClient，用于发送 HTTP 请求到正在测试的应用。
	 */
	@Autowired
	private WebTestClient webTestClient;

	// --- 测试数据 ---
	/**
	 * 一个已存在的书籍 ISBN，用于测试获取、更新等操作。
	 */
	private final String existingIsbn = "1234567890";
	/**
	 * 一个用于测试删除操作的书籍 ISBN。
	 */
	private final String isbnForDelete = "1234567892";
	/**
	 * 一个不存在的书籍 ISBN，用于测试资源未找到的场景。
	 */
	private final String nonExistentIsbn = "0987654321";

	/**
	 * 用于在 setUp 中创建的书籍对象。
	 */
	private Book bookToCreate = Book.of(existingIsbn, "Title", "Author", 9.90, "Publisher");
	/**
	 * 用于测试更新操作的书籍对象 (与 bookToCreate 具有相同的 ISBN)。
	 */
	private Book bookToUpdate = Book.of(existingIsbn, "Updated Title", "Updated Author", 19.90, "Publisher");
	/**
	 * 另一个用于在 setUp 中创建的书籍对象，主要用于后续的删除测试。
	 */
	private Book bookToDelete = Book.of(isbnForDelete, "Title for delete", "Author for delete", 9.90, "Publisher");

	/**
	 * 在所有测试方法运行之前执行一次。
	 * 用于初始化测试数据，通过 POST 请求创建两本书籍。
	 */
	@BeforeAll
	public void setUp() {
		// 清除测试数据
		webTestClient.delete().uri("/books/" + existingIsbn)
				.exchange();
				
		webTestClient.delete().uri("/books/" + isbnForDelete)
				.exchange();

		// 创建第一本书 (existingIsbn)
		webTestClient.post().uri("/books")
				.bodyValue(bookToCreate)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Book.class)
				.value(actualBook -> {
					assertThat(actualBook).isNotNull();
					assertThat(actualBook.isbn()).isEqualTo(bookToCreate.isbn());
				});

		// 创建第二本书 (isbnForDelete)
		webTestClient.post().uri("/books")
				.bodyValue(bookToDelete)
				.exchange()
				.expectStatus().isCreated()
				.expectBody(Book.class)
				.value(actualBook -> {
					assertThat(actualBook).isNotNull();
					assertThat(actualBook.isbn()).isEqualTo(bookToDelete.isbn());
				});
	}

	/**
	 * 在所有测试方法运行之后执行一次。
	 * 用于清理测试数据和验证最终状态。
	 * 首先验证更新和删除操作后的书籍列表状态，然后删除测试中更新过的书籍。
	 */
	@AfterAll
	public void tearDown() {
		webTestClient.get().uri("/books")
				.exchange()
				.expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<List<Book>>() {
				})
				.value(books -> {
					assertThat(books).isNotNull();
					assertThat(books.size()).isEqualTo(1);
					assertThat(books.stream().anyMatch(book -> book.isbn().equals(bookToUpdate.isbn()))).isTrue();
				});

		webTestClient.delete().uri("/books/" + existingIsbn)
				.exchange()
				.expectStatus().isNoContent();
	}

	/**
	 * 测试场景：当通过 GET 请求 /books 时，应返回书籍列表。
	 */
	@Test
	void whenGetAllBooksThenReturnBookList() {
		webTestClient.get().uri("/books")
				.exchange()
				.expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<List<Book>>() {
				})
				.value(books -> {
					assertThat(books).isNotNull();
					assertThat(books.size()).isGreaterThanOrEqualTo(1);
					assertThat(books.stream().anyMatch(book -> book.isbn().equals(existingIsbn))).isTrue();
				});
	}

	/**
	 * 测试场景：当通过 GET 请求 /books/{isbn} 使用一个已存在的 ISBN 时，应返回对应的书籍。
	 */
	@Test
	void whenGetBookByIsbnThenReturnBook() {
		webTestClient
				.get().uri("/books/" + existingIsbn)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Book.class).value(actualBook -> {
					assertThat(actualBook).isNotNull();
					assertThat(actualBook.isbn()).isEqualTo(existingIsbn);
				});
	}

	/**
	 * 测试场景：当通过 GET 请求 /books/{isbn} 使用一个不存在的 ISBN 时，应返回 404 Not Found。
	 */
	@Test
	void whenGetBookByIsbnNotFoundThenReturnNotFound() {
		webTestClient
				.get().uri("/books/" + nonExistentIsbn)
				.exchange()
				.expectStatus().isNotFound();
	}

	/**
	 * 测试场景：当通过 POST 请求 /books 尝试创建一本已存在的书籍 (ISBN重复) 时，
	 * 应返回 422 Unprocessable Entity (或其他表示业务逻辑错误的码，具体取决于异常处理配置)。
	 */
	@Test
	void whenPostExistingBookThenReturnUnprocessableEntity() {
		// bookToCreate is already created in @BeforeEach
		webTestClient.post().uri("/books")
				.bodyValue(bookToCreate) // 尝试再次创建同一本书
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY); // 期望 422 状态码
	}

	/**
	 * 测试场景：当通过 PUT 请求 /books/{isbn} 更新一本已存在的书籍时，
	 * 应返回 200 OK 和更新后的书籍信息。
	 */
	@Test
	void whenPutBookThenReturnUpdatedBook() {
		webTestClient.put().uri("/books/" + bookToUpdate.isbn())
				.bodyValue(bookToUpdate)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Book.class)
				.value(actualBook -> {
					assertThat(actualBook).isNotNull();
					assertThat(actualBook.isbn()).isEqualTo(bookToUpdate.isbn());
					assertThat(actualBook.title()).isEqualTo(bookToUpdate.title());
					assertThat(actualBook.author()).isEqualTo(bookToUpdate.author());
					assertThat(actualBook.price()).isEqualTo(bookToUpdate.price());
					assertThat(actualBook.publisher()).isEqualTo(bookToUpdate.publisher());
				});
	}

	/**
	 * 测试场景：当通过 DELETE 请求 /books/{isbn} 删除一本已存在的书籍时，
	 * 应返回 204 No Content。
	 */
	@Test
	void whenDeleteBookThenReturnNoContent() {
		webTestClient.delete().uri("/books/" + bookToDelete.isbn())
				.exchange()
				.expectStatus().isNoContent();
	}

	/**
	 * 测试场景：当通过 DELETE 请求 /books/{isbn} 尝试删除一本不存在的书籍时，
	 * 应返回 404 Not Found。
	 */
	@Test
	void whenDeleteNonExistentBookThenReturnNotFound() {
		webTestClient.delete().uri("/books/" + nonExistentIsbn)
				.exchange()
				.expectStatus().isNotFound();
	}
}