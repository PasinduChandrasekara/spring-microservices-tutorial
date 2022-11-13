package com.odyzzy.productcompositeservice.services;

import com.odyzzy.api.composite.product.*;
import com.odyzzy.api.core.product.Product;
import com.odyzzy.api.core.recommendation.Recommendation;
import com.odyzzy.api.core.review.Review;
import com.odyzzy.api.exceptions.NotFoundException;
import com.odyzzy.util.http.ServiceUtil;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private ProductCompositeIntegration productCompositeIntegration;

    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration productCompositeIntegration) {
        this.serviceUtil = serviceUtil;
        this.productCompositeIntegration = productCompositeIntegration;
    }

    @Override
    public ProductAggregate getProduct(int productId) {
        Product product = productCompositeIntegration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No Product found for ProductId: " + productId);
        }
        List<Recommendation> recommendations = productCompositeIntegration.getRecommendations(productId);
        List<Review> reviews = productCompositeIntegration.getReviews(productId);
        return createProductAggregate(product, reviews, recommendations, serviceUtil.getServiceAddress());
    }

    private ProductAggregate createProductAggregate(
            Product product,
            List<Review> reviews,
            List<Recommendation> recommendations,
            String serviceAddress) {
        int productId = product.getProductId();
        String productName = product.getName();
        int weight = product.getWeight();

        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null : recommendations.stream()
                        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
                        .collect(Collectors.toList());

        List<ReviewSummary> reviewSummaries =
                (reviews == null) ? null : reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject())).collect(Collectors.toList());

        String productAddress = product.getServiceAddress();
        String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ? recommendations.get(0).getServiceAddress() : null;
        String reviewAddress = (reviews != null && !reviews.isEmpty()) ? reviews.get(0).getServiceAddress() : null;

        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, productName, weight, recommendationSummaries, reviewSummaries, serviceAddresses);

    }
}
