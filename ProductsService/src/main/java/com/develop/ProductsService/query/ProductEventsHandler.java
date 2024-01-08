package com.develop.ProductsService.query;

import com.develop.ProductsService.core.data.ProductEntity;
import com.develop.ProductsService.core.data.ProductsRepository;
import com.develop.ProductsService.core.events.ProductCreatedEvent;
import com.development.core.events.ProductReserveEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductEventsHandler {

    private final ProductsRepository productsRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventsHandler.class);

    public ProductEventsHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @EventHandler
    public void on(ProductCreatedEvent event) {

        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event, productEntity);

        productsRepository.save(productEntity);
    }

    @EventHandler
    public void on(ProductReserveEvent event) {
        ProductEntity entity = productsRepository.findByProductId(event.getProductId());
        entity.setQuantity(entity.getQuantity() - event.getQuantity());
        productsRepository.save(entity);

        LOGGER.info("ProductReserveEvent is called for orderId: " + event.getOrderId() +
                "and productId: " + event.getProductId());
    }
}
