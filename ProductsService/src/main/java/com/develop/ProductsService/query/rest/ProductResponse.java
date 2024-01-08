package com.develop.ProductsService.query.rest;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponse {
    private String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;
}
