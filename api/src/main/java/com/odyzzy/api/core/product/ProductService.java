package com.odyzzy.api.core.product;

import org.springframework.web.bind.annotation.*;

public interface ProductService {

    @GetMapping(
            value = "/products/{productId}",
            produces = "application/json"
    )
    Product getProduct(@PathVariable int productId);

    @PostMapping(
            value = "/products",
            consumes = "application/json",
            produces = "application/json"
    )
    Product createProduct(@RequestBody Product product);

    @DeleteMapping(value = "/products/{productId}")
    void deleteProduct(@PathVariable int productId);
}
