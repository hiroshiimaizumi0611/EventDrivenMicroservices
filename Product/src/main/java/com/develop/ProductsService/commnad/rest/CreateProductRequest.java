package com.develop.ProductsService.commnad.rest;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CreateProductRequest {
    private String title;
    private BigDecimal price;
    private Integer quantity;
}
