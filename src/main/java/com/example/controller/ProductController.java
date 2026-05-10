package com.example.controller;

import com.example.dto.ProductListDTO;
import com.example.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:8081")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductListDTO>> list(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(productService.listProducts(categoryId, keyword));
    }
}
