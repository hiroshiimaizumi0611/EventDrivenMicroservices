package com.development.OrdersService.core.events;

import com.development.OrdersService.core.models.OrderStatus;
import lombok.Value;

@Value
public class OrderRejectedEvent {
    String orderId;
    String reason;
    OrderStatus orderStatus = OrderStatus.REJECTED;
}
