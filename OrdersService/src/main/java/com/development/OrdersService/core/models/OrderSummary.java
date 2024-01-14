package com.development.OrdersService.core.models;

import lombok.Value;

@Value
public class OrderSummary {
    String orderId;
    OrderStatus orderStatus;
    String message;
}
