package com.development.OrdersService.saga;

import com.development.OrdersService.core.events.OrderCreatedEvent;
import com.development.core.commands.ReserveProductCommand;
import com.development.core.events.ProductReserveEvent;
import com.development.core.models.User;
import com.development.core.query.FetchUserPaymentDetailsQuery;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

@Saga
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent event) {

        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(event.getOrderId())
                .productId(event.getProductId())
                .quantity(event.getQuantity())
                .userId(event.getUserId())
                .build();

        LOGGER.info("OrderCreatedEvent handled for orderId: " + reserveProductCommand.getOrderId() +
                "and productId: " + reserveProductCommand.getProductId());

        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {
            @Override
            public void onResult(@Nonnull CommandMessage<? extends ReserveProductCommand> commandMessage, @Nonnull CommandResultMessage<?> commandResultMessage) {
                if (commandResultMessage.isExceptional()) {
                    // Start compensating transaction
                }
            }
        });
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReserveEvent event) {
        LOGGER.info("ProductReserveEvent handled for orderId: " + event.getOrderId() +
                "and productId: " + event.getProductId());

        FetchUserPaymentDetailsQuery query = new FetchUserPaymentDetailsQuery(event.getUserId());

        User user;

        try {
            user = queryGateway.query(query, ResponseTypes.instanceOf(User.class)).join();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            // Start compensating transaction
            return;
        }

        if (user == null) {
            // Start compensating transaction
            return;
        }

        LOGGER.info("Successfully fetched user payment details for user " + user.getFirstName());
    }
}
