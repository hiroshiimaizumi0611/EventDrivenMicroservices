package com.develop.ProductsService.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final Environment env;

    public ProductsController(Environment env) {
        this.env = env;
    }

    @PostMapping
    public String createProduct(@RequestBody CreateProductRequest request) {
        return "HTTP POST Handled : " + request.getTitle();
    }

    @GetMapping
    public String getProduct() {
        return "HTTP GET Handled : " + env.getProperty("local.server.port");
    }

    @PutMapping
    public String updateProduct() {
        return "HTTP PUT Handled";
    }

    @DeleteMapping
    public String deleteProduct() {
        return "HTTP DELETE Handled";
    }
}
