package com.polarbookshop.catalog_service.domain;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record Book(
    @Id
    Long id,

    @NotBlank(message = "The book ISBN must be defined.")
    @Pattern(
        regexp = "^([0-9]{10}|[0-9]{13})$",
        message = "The ISBN format must be valid (10 or 13 digits)."
    )
    String isbn,

    @NotBlank(message = "The book title must be defined.")
    String title,

    @NotBlank(message = "The book author must be defined.")
    String author,

    @NotNull(message = "The book price must be defined.")
    @PositiveOrZero(message = "The book price must be zero or positive.")
    Double price,

    String publisher,
    
    @Version
    int version,

    @CreatedDate
    Instant createdDate,

    @LastModifiedDate
    Instant lastModifiedDate
) {
    public static Book of(String isbn, String title, String author, Double price, String publisher) {
        return new Book(null, isbn, title, author, price, publisher, 0, null, null);
    }
}
