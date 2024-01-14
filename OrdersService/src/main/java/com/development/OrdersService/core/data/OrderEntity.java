package com.development.OrdersService.core.data;

import com.development.OrdersService.core.models.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "orders")
@Data
public class OrderEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 5313493413859894403L;

    @Id
    @Column(unique = true)
    public String orderId;
    private String productId;
    private String userId;
    private int quantity;
    private String addressId;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
}
