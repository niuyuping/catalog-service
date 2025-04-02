package com.polarbookshop.catalog_service.domain;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BookValidationTests {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        // Initialize the validator factory and validator once for all tests
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsCorrectThenValidationSucceeds() {
        var book = Book.of("1234567890", "Title", "Author", 9.90);
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).isEmpty(); // Expect no validation violations
    }

    @Test
    void whenIsbnDefinedButIncorrectThenValidationFails() {
        var book = Book.of("a234567890", "Title", "Author", 9.90); // Invalid ISBN format
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The ISBN format must be valid (10 or 13 digits).");
    }

    @Test
    void whenIsbnNotDefinedThenValidationFails() {
        var book = Book.of("", "Title", "Author", 9.90); // Blank ISBN
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(2);
        Set<String> violationMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
        assertThat(violationMessages).containsExactlyInAnyOrder(
                "The book ISBN must be defined.",
                "The ISBN format must be valid (10 or 13 digits).");
    }

    @Test
    void whenTitleIsNotDefinedThenValidationFails() {
        var book = Book.of("1234567890", "", "Author", 9.90); // Blank Title
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The book title must be defined.");
    }

    @Test
    void whenAuthorIsNotDefinedThenValidationFails() {
        var book = Book.of("1234567890", "Title", "", 9.90); // Blank Author
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The book author must be defined.");
    }

    @Test
    void whenPriceIsNotDefinedThenValidationFails() {
        var book = Book.of("1234567890", "Title", "Author", null); // null Price
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The book price must be defined.");
    }

    @Test
    void whenPriceIsNegativeThenValidationFails() {
        var book = Book.of("1234567890", "Title", "Author", -9.90); // Negative Price
        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("The book price must be zero or positive.");
    }

    // Add more tests here for invalid cases...

}