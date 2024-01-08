package com.develop.ProductsService.query;

import com.develop.ProductsService.core.data.ProductEntity;
import com.develop.ProductsService.core.data.ProductsRepository;
import com.develop.ProductsService.query.rest.ProductResponse;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductsQueryHandler {

    private final ProductsRepository productsRepository;

    public ProductsQueryHandler(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @QueryHandler
    public List<ProductResponse> findProducts(FindProductsQuery query) {

        List<ProductResponse> products = new ArrayList<>();
        List<ProductEntity> storedProducts = productsRepository.findAll();

        for (ProductEntity entity : storedProducts) {
            ProductResponse productResponse = new ProductResponse();
            BeanUtils.copyProperties(entity, productResponse);
            products.add(productResponse);
        }

        return products;
    }
}
