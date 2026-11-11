package com.example.observability.service;

import com.example.observability.exception.ResourceNotFoundException;
import com.example.observability.model.Product;
import com.example.observability.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final Counter createdCounter;
    private final Counter deletedCounter;

    public ProductService(ProductRepository repository, MeterRegistry registry) {
        this.repository = repository;

        this.createdCounter = Counter.builder("products.created")
                .description("Number of products created")
                .register(registry);
        this.deletedCounter = Counter.builder("products.deleted")
                .description("Number of products deleted")
                .register(registry);
        Gauge.builder("products.total", repository, ProductRepository::count)
                .description("Current number of products in the database")
                .register(registry);
    }

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    public Product create(Product product) {
        product.setId(null);
        Product saved = repository.save(product);
        createdCounter.increment();
        return saved;
    }

    public Product update(Long id, Product incoming) {
        Product existing = findById(id);
        existing.setName(incoming.getName());
        existing.setPrice(incoming.getPrice());
        existing.setQuantity(incoming.getQuantity());
        return repository.save(existing);
    }

    public void delete(Long id) {
        Product existing = findById(id);
        repository.delete(existing);
        deletedCounter.increment();
    }
}
