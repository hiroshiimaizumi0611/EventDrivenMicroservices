package com.develop.ProductsService.query;

import com.develop.ProductsService.core.data.ProductEntity;
import com.develop.ProductsService.core.data.ProductsRepository;
import com.develop.ProductsService.core.events.ProductCreatedEvent;
import com.development.core.events.ProductReservationCanceledEvent;
import com.development.core.events.ProductReserveEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
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

        LOGGER.debug("ProductReservedEvent: Current product quantity " + entity.getQuantity());

        entity.setQuantity(entity.getQuantity() - event.getQuantity());
        productsRepository.save(entity);

        LOGGER.debug("ProductReservedEvent: New product quantity " + entity.getQuantity());
        LOGGER.info("ProductReserveEvent is called for orderId: " + event.getOrderId() +
                "and productId: " + event.getProductId());
    }

    @EventHandler
    public void on(ProductReservationCanceledEvent event) {
        ProductEntity entity = productsRepository.findByProductId(event.getProductId());

        LOGGER.debug("ProductReservationCanceledEvent: Current product quantity " + entity.getQuantity());

        int newQuantity = entity.getQuantity() + event.getQuantity();
        entity.setQuantity(newQuantity);
        productsRepository.save(entity);

        LOGGER.debug("ProductReservationCanceledEvent: New product quantity " + entity.getQuantity());
    }

    @ResetHandler
    public void reset() {
        productsRepository.deleteAll();
    }
}
