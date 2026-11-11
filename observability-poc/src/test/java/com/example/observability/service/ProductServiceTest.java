package com.example.observability.service;

import com.example.observability.exception.ResourceNotFoundException;
import com.example.observability.model.Product;
import com.example.observability.repository.ProductRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    private SimpleMeterRegistry registry;
    private ProductService service;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        service = new ProductService(repository, registry);
    }

    @Test
    void createIncrementsCounter() {
        Product input = new Product("Pen", new BigDecimal("10.00"), 5);
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        service.create(input);

        assertEquals(1.0, registry.get("products.created").counter().count());
    }

    @Test
    void findAllReturnsRepositoryData() {
        when(repository.findAll()).thenReturn(List.of(new Product("Pen", BigDecimal.TEN, 1)));

        assertEquals(1, service.findAll().size());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
    }

    @Test
    void updateChangesFields() {
        Product existing = new Product("Pen", BigDecimal.TEN, 1);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product updated = service.update(1L, new Product("Pencil", new BigDecimal("5"), 9));

        assertEquals("Pencil", updated.getName());
        assertEquals(9, updated.getQuantity());
    }

    @Test
    void deleteIncrementsCounter() {
        Product existing = new Product("Pen", BigDecimal.TEN, 1);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(repository).delete(existing);
        assertEquals(1.0, registry.get("products.deleted").counter().count());
    }
}
