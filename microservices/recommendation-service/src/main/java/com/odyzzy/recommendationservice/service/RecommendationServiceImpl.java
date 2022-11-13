package com.odyzzy.recommendationservice.service;

import com.odyzzy.api.core.recommendation.Recommendation;
import com.odyzzy.api.core.recommendation.RecommendationService;
import com.odyzzy.api.exceptions.InvalidInputException;
import com.odyzzy.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final ServiceUtil serviceUtil;

    public RecommendationServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid ProductId: " + productId);
        }
        if (productId == 113) {
            LOGGER.debug("No Recommendations found for ProductId {}", productId);
            return Collections.emptyList();
        }
        return getDummyRecommendations(productId);
    }

    private List<Recommendation> getDummyRecommendations(int productId) {
        List<Recommendation> recommendations = new ArrayList<>();
        recommendations.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
        recommendations.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
        recommendations.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));
        return recommendations;
    }
}
