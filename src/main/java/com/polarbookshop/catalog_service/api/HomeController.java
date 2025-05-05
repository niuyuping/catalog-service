package com.polarbookshop.catalog_service.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HomeController {

    @GetMapping("/")
    public Mono<String> getGreeting() {
        return Mono.just("Welcome to the book catalog!");
    }
}
