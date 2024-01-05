package com.develop.ProductsService.core.events;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductCreatedEvent {
    private String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;
}
