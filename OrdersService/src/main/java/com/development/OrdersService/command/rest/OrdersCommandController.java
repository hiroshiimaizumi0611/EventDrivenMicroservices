package com.development.OrdersService.command.rest;

import com.development.OrdersService.command.CreateOrderCommand;
import com.development.OrdersService.command.OrderStatus;
import jakarta.validation.Valid;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersCommandController {

    private final CommandGateway commandGateway;

    @Autowired
    public OrdersCommandController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public String createOrder(@Valid @RequestBody CreateOrderRequest request) {

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

        return commandGateway.sendAndWait(command);
    }
}
