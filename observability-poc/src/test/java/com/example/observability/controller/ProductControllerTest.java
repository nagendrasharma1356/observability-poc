package com.example.observability.controller;

import com.example.observability.exception.ResourceNotFoundException;
import com.example.observability.model.Product;
import com.example.observability.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService service;

    @Test
    void getAllReturnsList() throws Exception {
        when(service.findAll()).thenReturn(List.of(new Product("Pen", BigDecimal.TEN, 3)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pen"));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        when(service.findById(5L)).thenThrow(new ResourceNotFoundException("missing"));

        mockMvc.perform(get("/api/products/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createReturns201() throws Exception {
        when(service.create(any(Product.class))).thenReturn(new Product("Pen", BigDecimal.TEN, 3));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Pen\",\"price\":10,\"quantity\":3}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createRejectsBlankName() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":10,\"quantity\":3}"))
                .andExpect(status().isBadRequest());
    }
}
