package com.odyzzy.api.core.recommendation;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RecommendationService {

    @GetMapping(
            value = "/recommendations",
            produces = "application/json"
    )
    List<Recommendation> getRecommendations(@RequestParam(value = "productId") int productId);

    @PostMapping(
            value = "/recommendations",
            consumes = "application/json",
            produces = "application/json"
    )
    Recommendation createRecommendation(@RequestBody Recommendation recommendation);

    @DeleteMapping(value = "/recommendations")
    void deleteRecommendation(@RequestParam(value = "productId") int productId);
}
