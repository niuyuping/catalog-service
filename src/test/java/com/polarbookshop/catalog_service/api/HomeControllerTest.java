package com.polarbookshop.catalog_service.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(HomeController.class) // 指定测试目标为 HomeController
class HomeControllerTest {

    @Autowired
    private WebTestClient webClient; // 注入 WebTestClient 用于发送 HTTP 请求

    @Test
    void whenGetRootThenReturnWelcomeMessage() {
        webClient
            .get().uri("/") // 发送 GET 请求到 "/"
            .exchange() // 执行请求
            .expectStatus().isOk() // 验证 HTTP 状态码为 200 OK
            .expectBody(String.class) // 期望响应体是 String 类型
            .isEqualTo("Welcome to the book catalog!"); // 验证响应体内容
    }
} 