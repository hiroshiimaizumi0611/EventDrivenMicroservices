package com.develop.ProductsService.rest;

import com.develop.ProductsService.commnad.CreateProductCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final Environment env;
    private final CommandGateway commandGateway;

    @Autowired
    public ProductsController(Environment env, CommandGateway commandGateway) {
        this.env = env;
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public String createProduct(@RequestBody CreateProductRequest request) {

        CreateProductCommand command = CreateProductCommand.builder()
                .title(request.getTitle())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .productId(UUID.randomUUID().toString())
                .build();

        try {
            return commandGateway.sendAndWait(command);
        } catch (Exception ex) {
            return ex.getLocalizedMessage();
        }
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
