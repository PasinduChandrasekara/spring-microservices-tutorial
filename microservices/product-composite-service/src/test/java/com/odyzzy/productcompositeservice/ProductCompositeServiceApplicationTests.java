package com.odyzzy.productcompositeservice;

import com.odyzzy.api.core.product.Product;
import com.odyzzy.api.core.recommendation.Recommendation;
import com.odyzzy.api.core.review.Review;
import com.odyzzy.api.exceptions.InvalidInputException;
import com.odyzzy.api.exceptions.NotFoundException;
import com.odyzzy.productcompositeservice.services.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductCompositeServiceApplicationTests {

    private static final int PRODUCT_ID_OK = 1;
    private static final int PRODUCT_ID_NOT_FOUND = 2;
    private static final int PRODUCT_ID_INVALID = 3;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ProductCompositeIntegration productCompositeIntegration;

    @Test
    void contextLoads() {
    }

    @BeforeEach
    void setUp() {
        when(productCompositeIntegration.getProduct(PRODUCT_ID_OK))
                .thenReturn(new Product(PRODUCT_ID_OK, "Name", 1, "mock-service"));
        when(productCompositeIntegration.getReviews(PRODUCT_ID_OK))
                .thenReturn(Collections.singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock-service")));
		when(productCompositeIntegration.getRecommendations(PRODUCT_ID_OK))
				.thenReturn(Collections.singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock-service")));

        when(productCompositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
                .thenThrow(new NotFoundException("Not Found: "+ PRODUCT_ID_NOT_FOUND));
        when(productCompositeIntegration.getProduct(PRODUCT_ID_INVALID))
                .thenThrow(new InvalidInputException("Invalid: " + PRODUCT_ID_INVALID));
    }

    @Test
    void getProductById() {
        webTestClient.get()
                .uri("/product-composite/" + PRODUCT_ID_OK)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
                .jsonPath("$.recommendations.length()").isEqualTo(1)
                .jsonPath("$.reviews.length()").isEqualTo(1);
    }

    @Test
    void getProductNotFound() {
        webTestClient.get()
                .uri("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
                .jsonPath("$.message").isEqualTo("Not Found: " + PRODUCT_ID_NOT_FOUND);
    }

    @Test
    void getProductInvalidInput() {
        webTestClient.get()
                .uri("/product-composite/" + PRODUCT_ID_INVALID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
                .jsonPath("$.message").isEqualTo("Invalid: " + PRODUCT_ID_INVALID);
    }
}
