package com.example.observability.legacy;

import com.example.observability.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Only CSV and TEXT are tested on purpose - the HTML branch stays uncovered
 * so the SonarQube coverage metric shows a gap you can then close.
 */
class LegacyReportGeneratorTest {

    private final LegacyReportGenerator generator = new LegacyReportGenerator();
    private final List<Product> products = List.of(new Product("Pen", new BigDecimal("10"), 2));

    @Test
    void csvContainsName() {
        assertTrue(generator.generate(products, "CSV").contains("Pen,10"));
    }

    @Test
    void textContainsLabel() {
        assertTrue(generator.generate(products, "TEXT").contains("Product: Pen"));
    }

    @Test
    void adminCheck() {
        assertTrue(generator.isAdmin("admin123"));
        assertFalse(generator.isAdmin("wrong"));
    }
}
