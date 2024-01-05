package com.develop.ProductsService.query.rest;

import com.develop.ProductsService.query.FindProductsQuery;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsQueryController {

    private final QueryGateway queryGateway;

    public ProductsQueryController(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping
    public List<ProductResponse> getProducts() {
        FindProductsQuery findProductsQuery = new FindProductsQuery();

        return queryGateway.query(findProductsQuery,
                ResponseTypes.multipleInstancesOf(ProductResponse.class)).join();
    }
}
