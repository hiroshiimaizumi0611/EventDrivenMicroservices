package com.development.PaymentsService.query;

import com.development.PaymentsService.core.PaymentEntity;
import com.development.PaymentsService.core.PaymentsRepository;
import com.development.core.events.PaymentProcessedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventsHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(PaymentEventsHandler.class);
    private final PaymentsRepository paymentsRepository;

    public PaymentEventsHandler(PaymentsRepository paymentsRepository) {
        this.paymentsRepository = paymentsRepository;
    }

    @EventHandler
    public void on(PaymentProcessedEvent event) {
        LOGGER.info("PaymentProcessedEvent is called for orderId: " + event.getOrderId());

        PaymentEntity entity = new PaymentEntity();
        BeanUtils.copyProperties(event, entity);

        paymentsRepository.save(entity);
    }
}
