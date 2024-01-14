package com.development.OrdersService.command.rest;

import com.development.OrdersService.command.CreateOrderCommand;
import com.development.OrdersService.core.models.OrderStatus;
import com.development.OrdersService.core.models.OrderSummary;
import com.development.OrdersService.query.FindOrderQuery;
import jakarta.validation.Valid;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersCommandController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    public OrdersCommandController(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping
    public OrderSummary createOrder(@Valid @RequestBody CreateOrderRequest request) {

        String userId = "27b95829-4f3f-4ddf-8983-151ba010e35b";
        String orderId = UUID.randomUUID().toString();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .addressId(request.getAddressId())
                .productId(request.getProductId())
                .userId(userId)
                .quantity(request.getQuantity())
                .orderId(orderId)
                .orderStatus(OrderStatus.CREATED)
                .build();

        try (SubscriptionQueryResult<OrderSummary, OrderSummary> result = queryGateway.subscriptionQuery(new FindOrderQuery(orderId),
                ResponseTypes.instanceOf(OrderSummary.class),
                ResponseTypes.instanceOf(OrderSummary.class))) {
            commandGateway.sendAndWait(command);
            return result.updates().blockFirst();
        }
    }
}
