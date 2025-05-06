package com.polarbookshop.catalog_service.domain;

import static com.polarbookshop.catalog_service.domain.Book.ISBN_NOT_BLANK_MESSAGE;
import static com.polarbookshop.catalog_service.domain.Book.ISBN_PATTERN_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class BookValidationTests {

  private static Validator validator;

  @BeforeAll
  public static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void whenAllFieldsCorrectThenValidationSucceeds() {
    var book = new Book("1234567890", "Test Book", "Test Author", 9.90);
    Set<ConstraintViolation<Book>> violations = validator.validate(book);
    assertThat(violations).isEmpty();
  }

  static Stream<Arguments> isbnValidationTestData() {
    return Stream.of(
      // Valid cases
      Arguments.of("1234567890", new String[] {}),      // Valid ISBN-10 format
      Arguments.of("123456789X", new String[] {}),       // Valid ISBN-10 format with uppercase X
      Arguments.of("9781234567890", new String[] {}),   // Valid ISBN-13 format
      
      // Invalid cases: NotBlank violations (and potentially Pattern violations as well)
      Arguments.of(null, new String[] { ISBN_NOT_BLANK_MESSAGE }), // Null ISBN, fails @NotBlank
      Arguments.of(
        "", // Empty ISBN
        new String[] { ISBN_PATTERN_MESSAGE, ISBN_NOT_BLANK_MESSAGE } // Fails @NotBlank and @Pattern
      ),
      Arguments.of(
        "   ", // ISBN with only whitespaces
        new String[] { ISBN_NOT_BLANK_MESSAGE, ISBN_PATTERN_MESSAGE } // Fails @NotBlank and @Pattern
      ),

      // Invalid cases: Pattern violations (NotBlank constraint passes for these)
      Arguments.of("123456789012X", new String[] { ISBN_PATTERN_MESSAGE }), // Invalid length (13 chars but ends with X)
      Arguments.of("123456789", new String[] { ISBN_PATTERN_MESSAGE }),       // Invalid: 9 digits (too short for ISBN-10 or ISBN-13)
      Arguments.of("12345678901", new String[] { ISBN_PATTERN_MESSAGE }),     // Invalid: 11 digits (not ISBN-10 or ISBN-13 length)
      Arguments.of("123456789012", new String[] { ISBN_PATTERN_MESSAGE }),    // Invalid: 12 digits (not ISBN-10 or ISBN-13 length)
      Arguments.of("97812345678901", new String[] { ISBN_PATTERN_MESSAGE }), // Invalid: 14 digits (too long for ISBN-13)
      Arguments.of("a234567890", new String[] { ISBN_PATTERN_MESSAGE }),      // Invalid: contains non-digit character 'a'
      Arguments.of("!@#$%^&*()_+,!", new String[] { ISBN_PATTERN_MESSAGE }), // Invalid: contains special characters
      Arguments.of("123456789x", new String[] { ISBN_PATTERN_MESSAGE }),      // Invalid: ISBN-10 with lowercase 'x'
      Arguments.of(" 1234567890", new String[] { ISBN_PATTERN_MESSAGE }),   // Invalid: leading whitespace prevents pattern match
      Arguments.of("1234567890 ", new String[] { ISBN_PATTERN_MESSAGE }),   // Invalid: trailing whitespace prevents pattern match
      Arguments.of(" 1234567890 ", new String[] { ISBN_PATTERN_MESSAGE }) // Invalid: leading and trailing whitespace prevents pattern match
    );
  }

  @ParameterizedTest
  @MethodSource("isbnValidationTestData")
  void whenIsbnIsProvidedThenValidationShouldSucceedOrFailAccordingly(
    String isbn,
    String[] expectedMessages
  ) {
    var book = new Book(isbn, "Test Title", "Test Author", 9.99);
    Set<ConstraintViolation<Book>> allViolations = validator.validate(book);

    List<ConstraintViolation<Book>> isbnViolations = allViolations
      .stream()
      .filter(v -> v.getPropertyPath().toString().equals("isbn"))
      .collect(Collectors.toList());

    assertThat(isbnViolations).hasSize(expectedMessages.length);

    if (expectedMessages.length > 0) {
      List<String> actualViolationMessages = isbnViolations
        .stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toList());

      assertThat(actualViolationMessages).containsExactlyInAnyOrder(
        expectedMessages
      );
    }
  }
}
