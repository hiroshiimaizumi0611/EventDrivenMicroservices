package com.development.OrdersService.core.events;

import com.development.OrdersService.core.models.OrderStatus;
import lombok.Value;

@Value
public class OrderApprovedEvent {
    String orderId;
    OrderStatus orderStatus = OrderStatus.APPROVED;
}
