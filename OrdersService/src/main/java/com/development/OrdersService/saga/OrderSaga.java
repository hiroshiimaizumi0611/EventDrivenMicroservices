package com.development.OrdersService.saga;

import com.development.OrdersService.command.ApproveOrderCommand;
import com.development.OrdersService.command.RejectOrderCommand;
import com.development.OrdersService.core.events.OrderApprovedEvent;
import com.development.OrdersService.core.events.OrderCreatedEvent;
import com.development.OrdersService.core.events.OrderRejectedEvent;
import com.development.core.commands.CancelProductReservationCommand;
import com.development.core.commands.ProcessPaymentCommand;
import com.development.core.commands.ReserveProductCommand;
import com.development.core.events.PaymentProcessedEvent;
import com.development.core.events.ProductReservationCanceledEvent;
import com.development.core.events.ProductReserveEvent;
import com.development.core.models.User;
import com.development.core.query.FetchUserPaymentDetailsQuery;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Saga
public class OrderSaga {

    private final transient CommandGateway commandGateway;

    private final transient QueryGateway queryGateway;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);

    public OrderSaga(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

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
            cancelProductReservation(event, ex.getMessage());
            return;
        }

        if (user == null) {
            cancelProductReservation(event, "Could not fetch user payment details.");
            return;
        }

        LOGGER.info("Successfully fetched user payment details for user " + user.getFirstName());

        ProcessPaymentCommand command = ProcessPaymentCommand.builder()
                .orderId(event.getOrderId())
                .paymentDetails(user.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();

        String result = null;

        try {
            result = commandGateway.sendAndWait(command, 10, TimeUnit.SECONDS);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            cancelProductReservation(event, ex.getMessage());
            return;
        }

        if (result == null) {
            LOGGER.info("The ProcessPaymentCommand resulted in NUll. Initiating a compensating transaction.");
            cancelProductReservation(event, "Could not process user payment with provided payment details.");
        }
    }

    private void cancelProductReservation(ProductReserveEvent event, String reason) {
        CancelProductReservationCommand command = CancelProductReservationCommand.builder()
                .orderId(event.getOrderId())
                .productId(event.getProductId())
                .quantity(event.getQuantity())
                .userId(event.getUserId())
                .reason(reason)
                .build();

        commandGateway.send(command);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent event) {
        ApproveOrderCommand command = new ApproveOrderCommand(event.getOrderId());
        commandGateway.send(command);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent event) {
        LOGGER.info("Order is approved. Order Saga is complete for orderId: " + event.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCanceledEvent event) {
        // Create and send a RejectProductCommand
        RejectOrderCommand command = new RejectOrderCommand(event.getOrderId(), event.getReason());

        commandGateway.send(command);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent event) {
        LOGGER.info("Successfully rejected order with id " + event.getOrderId());
    }
}
