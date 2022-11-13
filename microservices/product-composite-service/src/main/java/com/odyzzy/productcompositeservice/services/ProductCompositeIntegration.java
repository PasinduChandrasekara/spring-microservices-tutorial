package com.odyzzy.productcompositeservice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odyzzy.api.core.product.Product;
import com.odyzzy.api.core.product.ProductService;
import com.odyzzy.api.core.recommendation.Recommendation;
import com.odyzzy.api.core.recommendation.RecommendationService;
import com.odyzzy.api.core.review.Review;
import com.odyzzy.api.core.review.ReviewService;
import com.odyzzy.api.exceptions.InvalidInputException;
import com.odyzzy.api.exceptions.NotFoundException;
import com.odyzzy.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class ProductCompositeIntegration implements ProductService, ReviewService, RecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCompositeIntegration.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String productServiceUrl;
    private final String reviewServiceUrl;
    private final String recommendationServiceUrl;

    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${app.product-service.host}")
            String productServiceHost,
            @Value("${app.product-service.port}")
            int productServicePort,
            @Value("${app.review-service.host}")
            String reviewServiceHost,
            @Value("${app.review-service.port}")
            int reviewServicePort,
            @Value("${app.recommendation-service.host}")
            String recommendationServiceHost,
            @Value("${app.recommendation-service.port}")
            int recommendationServicePort) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;

        this.productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/products/";
        this.reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/reviews?productId=";
        this.recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendations?productId=";
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = this.productServiceUrl + productId;
            Product product = restTemplate.getForObject(url, Product.class);
            return product;
        } catch (HttpClientErrorException ex) {
            switch (ex.getStatusCode()) {
                case NOT_FOUND:
                    throw new NotFoundException(getErrorMessage(ex));
                case UNPROCESSABLE_ENTITY:
                    throw new InvalidInputException(getErrorMessage(ex));
                default:
                    LOGGER.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                    LOGGER.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
            }
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = this.recommendationServiceUrl + productId;
            List<Recommendation> recommendations = this.restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Recommendation>>() {
                    }).getBody();
            return recommendations;
        } catch (Exception ex) {
            LOGGER.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = this.reviewServiceUrl + productId;
            List<Review> reviews = this.restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Review>>() {
            }).getBody();
            return reviews;
        } catch (Exception ex) {
            LOGGER.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException e) {
            return e.getMessage();
        }
    }
}
