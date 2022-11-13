package com.odyzzy.api.core.review;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ReviewService {

    @GetMapping(
            value = "/reviews",
            produces = "application/json"
    )
    List<Review> getReviews(@RequestParam(value = "productId") int productId);

    @PostMapping(
            value = "/reviews",
            consumes = "application/json",
            produces = "application/json"
    )
    Review createReview(@RequestBody Review review);

    @DeleteMapping(value = "/reviews")
    void deleteReview(@RequestParam(value = "productId") int productId);
}
