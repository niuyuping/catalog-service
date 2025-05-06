package com.polarbookshop.catalog_service.api;

import com.polarbookshop.catalog_service.domain.BookAlreadyExistsException;
import com.polarbookshop.catalog_service.domain.BookNotFoundException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class BookControllerAdvice {

  @ExceptionHandler(BookAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public Mono<String> handleBookAlreadyExistsException(
    BookAlreadyExistsException ex
  ) {
    return Mono.just(ex.getMessage());
  }

  @ExceptionHandler(BookNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Mono<String> handleBookNotFoundException(BookNotFoundException ex) {
    return Mono.just(ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Mono<Map<String, String>> handleValidationException(
    MethodArgumentNotValidException ex
  ) {
    final Map<String, String> errors = ex
      .getBindingResult()
      .getAllErrors()
      .stream()
      .filter(FieldError.class::isInstance)
      .map(FieldError.class::cast)
      .collect(
        Collectors.toMap(
          FieldError::getField,
          FieldError::getDefaultMessage,
          (existingValue, newValue) -> existingValue
        )
      );
    return Mono.just(errors);
  }
}
