package com.polarbookshop.catalog_service.domain;

import com.polarbookshop.catalog_service.application.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BookServiceTests {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository);
    }

    @Test
    void whenBookExistsThenAddBookThrowsException() {
        // 准备测试数据
        var book = Book.of("1234567890", "Title", "Author", 9.90, "Publisher");

        // 模拟 BookRepository 的行为
        given(bookRepository.existsByIsbn(book.isbn()))
                .willReturn(Mono.just(true));

        // 尝试创建书籍
        Mono<Book> result = bookService.addBookToCatalog(book);

        // 使用 reactor-test 验证抛出 BookAlreadyExistsException
        StepVerifier.create(result)
                .expectError(BookAlreadyExistsException.class)
                .verify();
    }

    @Test
    void whenGetBookNotExistingThenThrowBookNotFoundException() {
        // 准备测试数据
        var nonExistentIsbn = "9876543210";

        // 模拟 BookRepository 的行为
        given(bookRepository.findByIsbn(nonExistentIsbn))
                .willReturn(Mono.empty());

        // 尝试获取书籍
        Mono<Book> result = bookService.viewBookDetails(nonExistentIsbn);

        // 使用 reactor-test 验证抛出 BookNotFoundException
        StepVerifier.create(result)
                .expectError(BookNotFoundException.class)
                .verify();
    }
}
