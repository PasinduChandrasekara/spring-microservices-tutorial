package com.odyzzy.productservice;

import com.odyzzy.productservice.persistence.ProductEntity;
import com.odyzzy.productservice.persistence.ProductRepository;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;

@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
public class PersistenceTests extends MongoDbTestBase {

    @Autowired
    private ProductRepository repository;

    private ProductEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        ProductEntity entity = new ProductEntity(1, "n", 1);
        savedEntity = repository.save(entity);
        assertEqualsProduct(entity, savedEntity);
    }

    @Test
    void create() {
        ProductEntity entity = new ProductEntity(2, "n", 2);
        repository.save(entity);

        ProductEntity foundEntity = repository.findByProductId(entity.getProductId()).get();
        assertEqualsProduct(entity, foundEntity);
        Assert.assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedEntity.setName("n2");
        repository.save(savedEntity);

        ProductEntity foundEntity = repository.findByProductId(savedEntity.getProductId()).get();
        Assert.assertEquals(1, (long) foundEntity.getVersion());
        Assert.assertEquals("n2", foundEntity.getName());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        Assert.assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());
        Assert.assertTrue(entity.isPresent());
        assertEqualsProduct(savedEntity, entity.get());
    }

    @Test
    void duplicateError() {
        Assert.assertThrows(DuplicateKeyException.class, () -> {
            ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
            repository.save(entity);
        });
    }

    @Test
    void optimisticLockError() {
        ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

        entity1.setName("n1");
        repository.save(entity1);

        Assert.assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setName("n2");
            repository.save(entity2);
        });

        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        Assert.assertEquals(1, updatedEntity.getVersion().intValue());
        Assert.assertEquals("n1", updatedEntity.getName());
    }

    @Test
    void paging() {
        repository.deleteAll();
        List<ProductEntity> newProducts = rangeClosed(1001, 1010)
                .mapToObj(i -> new ProductEntity(i, "name" + i, i))
                .collect(Collectors.toList());
        repository.saveAll(newProducts);

        Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "productId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }

    private void assertEqualsProduct(ProductEntity expected, ProductEntity actual) {
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getVersion(), actual.getVersion());
        Assert.assertEquals(expected.getProductId(), actual.getProductId());
        Assert.assertEquals(expected.getName(), actual.getName());
        Assert.assertEquals(expected.getWeight(), actual.getWeight());
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productPage = repository.findAll(nextPage);
        Assert.assertEquals(expectedProductIds, productPage.getContent().stream()
                .map(p -> p.getProductId()).collect(Collectors.toList()).toString());
        Assert.assertEquals(expectsNextPage, productPage.hasNext());
        return productPage.nextPageable();
    }
}
