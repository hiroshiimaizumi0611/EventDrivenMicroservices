package com.development.OrdersService.query;

import com.development.OrdersService.core.data.OrderEntity;
import com.development.OrdersService.core.data.OrdersRepository;
import com.development.OrdersService.core.models.OrderSummary;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class OrderQueriesHandler {
    private final OrdersRepository ordersRepository;

    public OrderQueriesHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @QueryHandler
    public OrderSummary findOrder(FindOrderQuery query) {
        OrderEntity entity = ordersRepository.findByOrderId(query.getOrderId());
        return new OrderSummary(entity.getOrderId(), entity.getOrderStatus(), "");
    }
}
