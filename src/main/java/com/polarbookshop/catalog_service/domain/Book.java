package com.polarbookshop.catalog_service.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
public record Book(
  @NotBlank(message = "The book ISBN must be defined.")
  @Pattern(
    regexp = "^([0-9]{9}[0-9X]|[0-9]{13})$",
    message = "The ISBN format must be valid (ISBN-10 or ISBN-13)."
  )
  String isbn,

  @NotBlank(message = "The book title must be defined.") String title,

  @NotBlank(message = "The book author must be defined.") String author,

  @NotNull(message = "The book price must be defined.")
  @DecimalMin(value = "0.00", inclusive = false, message = "The book price must be greater than zero.")
  @Digits(integer = 5, fraction = 2, message = "The book price must be a valid number with up to 2 decimal places.")
  Double price
) {
  public Book with(String title, String author, Double price) {
    return new Book(this.isbn, title, author, price);
  }
}
