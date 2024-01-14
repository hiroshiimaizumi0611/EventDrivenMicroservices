package com.development.OrdersService.saga;

import com.development.OrdersService.command.ApproveOrderCommand;
import com.development.OrdersService.command.RejectOrderCommand;
import com.development.OrdersService.core.events.OrderApprovedEvent;
import com.development.OrdersService.core.events.OrderCreatedEvent;
import com.development.OrdersService.core.events.OrderRejectedEvent;
import com.development.OrdersService.core.models.OrderSummary;
import com.development.OrdersService.query.FindOrderQuery;
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
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Saga
public class OrderSaga {

    @Autowired
    private transient CommandGateway commandGateway;
    @Autowired
    private transient QueryGateway queryGateway;
    @Autowired
    private transient DeadlineManager deadlineManager;
    @Autowired
    private transient QueryUpdateEmitter queryUpdateEmitter;

    private String scheduleId;

    private final static String PAYMENT_PROCESSING_TIMEOUT_DEADLINE = "payment-processing-deadline";

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);

//    public OrderSaga() {
//    }
//
//    @Autowired
//    public OrderSaga(CommandGateway commandGateway, QueryGateway queryGateway, DeadlineManager deadlineManager, QueryUpdateEmitter queryUpdateEmitter) {
//        this.commandGateway = commandGateway;
//        this.queryGateway = queryGateway;
//        this.deadlineManager = deadlineManager;
//        this.queryUpdateEmitter = queryUpdateEmitter;
//    }

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
                    RejectOrderCommand command = new RejectOrderCommand(event.getOrderId(), commandResultMessage.exceptionResult().getMessage());
                    commandGateway.send(command);
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

        scheduleId = deadlineManager.schedule(Duration.of(120, ChronoUnit.SECONDS), PAYMENT_PROCESSING_TIMEOUT_DEADLINE, user);

        ProcessPaymentCommand command = ProcessPaymentCommand.builder()
                .orderId(event.getOrderId())
                .paymentDetails(user.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();

        String result = null;

        try {
            result = commandGateway.sendAndWait(command);
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
        cancelDeadline();

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
        cancelDeadline();
        ApproveOrderCommand command = new ApproveOrderCommand(event.getOrderId());
        commandGateway.send(command);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent event) {
        LOGGER.info("Order is approved. Order Saga is complete for orderId: " + event.getOrderId());
        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true, new OrderSummary(event.getOrderId(), event.getOrderStatus(), ""));
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCanceledEvent event) {
        RejectOrderCommand command = new RejectOrderCommand(event.getOrderId(), event.getReason());
        commandGateway.send(command);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent event) {
        LOGGER.info("Successfully rejected order with id " + event.getOrderId());
        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true, new OrderSummary(event.getOrderId(), event.getOrderStatus(), event.getReason()));
    }

    @DeadlineHandler(deadlineName = PAYMENT_PROCESSING_TIMEOUT_DEADLINE)
    public void handlePaymentDeadline(ProductReserveEvent event) {
        LOGGER.info("Payment processing deadline took place. Sending a compensating command to cancel the product reservation.");
        cancelProductReservation(event, "Payment timeout");
    }

    private void cancelDeadline() {
        if (scheduleId != null) {
            deadlineManager.cancelSchedule(PAYMENT_PROCESSING_TIMEOUT_DEADLINE, scheduleId);
            scheduleId = null;
        }
    }
}
