package com.polarbookshop.catalog_service.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.polarbookshop.catalog_service.domain.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.TestPropertySource;

@JsonTest
@TestPropertySource(
  properties = "spring.jackson.databind.default-property-inclusion=non_null"
)
public class BookJsonTests {

  @Autowired
  private JacksonTester<Book> json;

  @Test
  void testSerialize() throws Exception {
    var book = new Book("1234567890", "Title", "Author", 9.90);
    var jsonContent = json.write(book);
    assertThat(jsonContent)
      .extractingJsonPathStringValue("@.isbn")
      .isEqualTo(book.isbn());
    assertThat(jsonContent)
      .extractingJsonPathStringValue("@.title")
      .isEqualTo(book.title());
    assertThat(jsonContent)
      .extractingJsonPathStringValue("@.author")
      .isEqualTo(book.author());
    assertThat(jsonContent)
      .extractingJsonPathNumberValue("@.price")
      .isEqualTo(book.price());
  }

  @Test
  void testDeserialize() throws Exception {
    var jsonContent =
      """
      {
          "isbn": "1234567890",
          "title": "Title",
          "author": "Author",
          "price": 9.90
      }
      """;
    assertThat(json.parse(jsonContent))
      .usingRecursiveComparison()
      .isEqualTo(new Book("1234567890", "Title", "Author", 9.90));
  }
}
