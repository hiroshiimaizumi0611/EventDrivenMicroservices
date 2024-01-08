package com.develop.ProductsService.commnad;

import com.develop.ProductsService.core.events.ProductCreatedEvent;
import com.development.core.commands.ReserveProductCommand;
import com.development.core.events.ProductReserveEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

@Aggregate
public class ProductAggregate {

    @AggregateIdentifier
    private String productId;
    private String title;
    private BigDecimal price;
    private Integer quantity;

    public ProductAggregate() {

    }

    @CommandHandler
    public ProductAggregate(CreateProductCommand command) {
        if (command.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price cannot be less or equal than zero.");
        }

        if (command.getTitle() == null || command.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }

        ProductCreatedEvent event = new ProductCreatedEvent();

        BeanUtils.copyProperties(command, event);

        AggregateLifecycle.apply(event);
    }

    @CommandHandler
    public void on(ReserveProductCommand command) {

        if (quantity < command.getQuantity()) {
            throw new IllegalArgumentException("Insufficient number of items in stock.");
        }

        ProductReserveEvent productReservedEvent = ProductReserveEvent.builder()
                .orderId(command.getOrderId())
                .productId(command.getProductId())
                .quantity(command.getQuantity())
                .userId(command.getUserId())
                .build();

        AggregateLifecycle.apply(productReservedEvent);
    }

    @EventSourcingHandler
    public void on(ProductCreatedEvent event) {
        this.productId = event.getProductId();
        this.price = event.getPrice();
        this.title = event.getTitle();
        this.quantity = event.getQuantity();
    }

    @EventSourcingHandler
    public void on(ProductReserveEvent event) {
        this.quantity -= event.getQuantity();
    }
}
