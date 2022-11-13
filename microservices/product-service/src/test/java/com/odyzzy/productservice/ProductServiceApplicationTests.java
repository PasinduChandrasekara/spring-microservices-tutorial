package com.odyzzy.productservice;

import com.odyzzy.api.core.product.Product;
import com.odyzzy.productservice.persistence.ProductRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests extends MongoDbTestBase {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private WebTestClient client;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();
    }

    @Test
    void duplicateError() {
        int productId = 1;
        postAndVerifyProduct(productId, HttpStatus.OK);
        Assert.assertTrue(repository.findByProductId(productId).isPresent());

        postAndVerifyProduct(productId, HttpStatus.UNPROCESSABLE_ENTITY)
                .jsonPath("$.path").isEqualTo("/products")
                .jsonPath("$.message").isEqualTo("Duplicate key, ProductId: " + productId);
    }

    private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
        Product product = new Product(productId, "Name " + productId, productId, "SA");
        return client.post()
                .uri("/products")
                .body(just(product), Product.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody();
    }

    private WebTestClient.BodyContentSpec getAndVerifyProduct(String productPath, HttpStatus expectedStatus) {
        return null;
    }

}
