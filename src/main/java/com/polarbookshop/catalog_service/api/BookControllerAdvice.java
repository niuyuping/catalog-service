package com.polarbookshop.catalog_service.api;

import com.polarbookshop.catalog_service.domain.BookAlreadyExistsException;
import com.polarbookshop.catalog_service.domain.BookNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // Indicates this class provides centralized exception handling for REST
                      // controllers
public class BookControllerAdvice {

    @ExceptionHandler(BookNotFoundException.class) // Handle BookNotFoundException
    @ResponseStatus(HttpStatus.NOT_FOUND) // Set HTTP status to 404
    public Mono<String> handleBookNotFound(BookNotFoundException ex) {
        return Mono.just(ex.getMessage()); // Return the exception message as the response body
    }

    @ExceptionHandler(BookAlreadyExistsException.class) // Handle BookAlreadyExistsException
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY) // Set HTTP status to 422 (Unprocessable Entity) - common for
                                                     // business rule violations like duplicates
    public Mono<String> handleBookAlreadyExists(BookAlreadyExistsException ex) {
        return Mono.just(ex.getMessage()); // Return the exception message
    }

    @ExceptionHandler(WebExchangeBindException.class) // Handle WebFlux validation/binding exceptions
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<Map<String, String>> handleValidationExceptions(WebExchangeBindException ex) {
        var errors = new HashMap<String, String>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return Mono.just(errors); // Return a map of field names to error messages
    }

}