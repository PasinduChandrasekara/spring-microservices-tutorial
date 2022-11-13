package com.odyzzy.productservice.services;

import com.odyzzy.api.core.product.Product;
import com.odyzzy.api.core.product.ProductService;
import com.odyzzy.api.exceptions.InvalidInputException;
import com.odyzzy.api.exceptions.NotFoundException;
import com.odyzzy.productservice.persistence.ProductEntity;
import com.odyzzy.productservice.persistence.ProductRepository;
import com.odyzzy.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductServiceImpl(ServiceUtil serviceUtil, ProductRepository repository, ProductMapper mapper) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Product getProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        ProductEntity entity = repository.findByProductId(productId).orElseThrow(() ->
                new NotFoundException("No product found for ProductId: " + productId));

        Product response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());
        return response;
    }

    @Override
    public Product createProduct(Product product) {
        try {
            ProductEntity entity = mapper.apiToEntity(product);
            ProductEntity savedEntity = repository.save(entity);
            return mapper.entityToApi(savedEntity);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, ProductId: " + product.getProductId());
        }
    }

    @Override
    public void deleteProduct(int productId) {
        repository.findByProductId(productId).ifPresent(e -> repository.delete(e));
    }


}
