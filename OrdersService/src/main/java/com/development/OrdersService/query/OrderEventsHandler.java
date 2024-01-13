package com.development.OrdersService.query;

import com.development.OrdersService.core.data.OrderEntity;
import com.development.OrdersService.core.data.OrdersRepository;
import com.development.OrdersService.core.events.OrderApprovedEvent;
import com.development.OrdersService.core.events.OrderCreatedEvent;
import com.development.OrdersService.core.events.OrderRejectedEvent;
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

    @EventHandler
    public void on(OrderApprovedEvent event) {

        OrderEntity entity = ordersRepository.findByOrderId(event.getOrderId());

        if (entity == null) {
            // TODO: Do something about it.
            return;
        }

        entity.setOrderStatus(event.getOrderStatus());

        ordersRepository.save(entity);
    }

    @EventHandler
    public void on(OrderRejectedEvent event) {
        OrderEntity entity = ordersRepository.findByOrderId(event.getOrderId());
        entity.setOrderStatus(event.getOrderStatus());
        ordersRepository.save(entity);
    }
}
