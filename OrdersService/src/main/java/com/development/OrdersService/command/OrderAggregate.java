package com.development.OrdersService.command;

import com.development.OrdersService.core.events.OrderApprovedEvent;
import com.development.OrdersService.core.events.OrderCreatedEvent;
import com.development.OrdersService.core.events.OrderRejectedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate
public class OrderAggregate {

    @AggregateIdentifier
    private String orderId;
    private String productId;
    private String userId;
    private int quantity;
    private String addressId;
    private OrderStatus orderStatus;

    public OrderAggregate() {

    }

    @CommandHandler
    public OrderAggregate(CreateOrderCommand command) {
        OrderCreatedEvent event = new OrderCreatedEvent();
        BeanUtils.copyProperties(command, event);
        AggregateLifecycle.apply(event);
    }

    @CommandHandler
    public void handle(ApproveOrderCommand command) {
        // OrderApprovedEvent
        OrderApprovedEvent orderApprovedEvent = new OrderApprovedEvent(command.getOrderId());
        AggregateLifecycle.apply(orderApprovedEvent);
    }

    @CommandHandler
    public void handle(RejectOrderCommand command) {
        OrderRejectedEvent event = new OrderRejectedEvent(command.getOrderId(), command.getReason());
        AggregateLifecycle.apply(event);
    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent event) {
        this.orderId = event.getOrderId();
        this.productId = event.getProductId();
        this.userId = event.getUserId();
        this.quantity = event.getQuantity();
        this.addressId = event.getAddressId();
        this.orderStatus = event.getOrderStatus();
    }

    @EventSourcingHandler
    public void on(OrderApprovedEvent event) {
        this.orderStatus = event.getOrderStatus();
    }

    @EventSourcingHandler
    public void on(OrderRejectedEvent event) {
        this.orderStatus = event.getOrderStatus();
    }
}
