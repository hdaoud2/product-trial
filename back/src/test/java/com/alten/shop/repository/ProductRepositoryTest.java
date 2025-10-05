package com.alten.shop.repository;

import com.alten.shop.entity.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void shouldSaveAndFindProduct() {
        // Given
        Product product = new Product();
        product.setCode("TEST123");
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setImage("test.jpg");
        product.setCategory("Electronics");
        product.setPrice(99.99);
        product.setQuantity(10);
        product.setInternalReference("REF-TEST");
        product.setShellId(1L);
        product.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        product.setRating(5);

        // When
        Product saved = productRepository.save(product);
        Optional<Product> found = productRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Product", found.get().getName());
        assertEquals("Electronics", found.get().getCategory());
    }

    @Test
    void shouldFindByCategory() {
        // Given
        Product product1 = createProduct("ELECTRO1", "Laptop", "Electronics", 999.99);
        Product product2 = createProduct("CLOTH1", "T-Shirt", "Clothing", 29.99);
        productRepository.save(product1);
        productRepository.save(product2);

        // When
        List<Product> electronics = productRepository.findByCategory("Electronics");

        // Then
        assertFalse(electronics.isEmpty());
        assertEquals(1, electronics.size());
        assertEquals("Laptop", electronics.get(0).getName());
    }

    @Test
    void shouldFindByPriceBetween() {
        // Given
        Product cheapProduct = createProduct("CHEAP1", "Cheap Item", "Misc", 10.0);
        Product expensiveProduct = createProduct("EXP1", "Expensive Item", "Misc", 1000.0);
        productRepository.save(cheapProduct);
        productRepository.save(expensiveProduct);

        // When
        List<Product> midRangeProducts = productRepository.findByPriceBetween(50.0, 500.0);

        // Then
        assertTrue(midRangeProducts.isEmpty());

        List<Product> affordableProducts = productRepository.findByPriceBetween(5.0, 50.0);
        assertEquals(1, affordableProducts.size());
        assertEquals("Cheap Item", affordableProducts.get(0).getName());
    }

    @Test
    void shouldFindByCode() {
        // Given
        Product product = createProduct("UNIQUE123", "Unique Product", "Test", 50.0);
        productRepository.save(product);

        // When
        Optional<Product> found = productRepository.findByCode("UNIQUE123");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Unique Product", found.get().getName());
    }

    private Product createProduct(String code, String name, String category, Double price) {
        Product product = new Product();
        product.setCode(code);
        product.setName(name);
        product.setDescription("Description for " + name);
        product.setImage("image.jpg");
        product.setCategory(category);
        product.setPrice(price);
        product.setQuantity(10);
        product.setInternalReference("REF-" + code);
        product.setShellId(1L);
        product.setInventoryStatus(Product.InventoryStatus.INSTOCK);
        product.setRating(4);
        return product;
    }
}