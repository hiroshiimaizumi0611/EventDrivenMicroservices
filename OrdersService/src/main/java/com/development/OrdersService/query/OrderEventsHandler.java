package com.development.OrdersService.query;

import com.development.OrdersService.core.data.OrderEntity;
import com.development.OrdersService.core.data.OrdersRepository;
import com.development.OrdersService.core.events.OrderCreatedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class OrderEventsHandler {

    private final OrdersRepository ordersRepository;

    public OrderEventsHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) {

        OrderEntity entity = new OrderEntity();
        BeanUtils.copyProperties(event, entity);

        ordersRepository.save(entity);
    }
}
