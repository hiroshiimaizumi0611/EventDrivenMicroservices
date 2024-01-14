package com.development.PaymentsService.core;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "payments")
@Data
public class PaymentEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 5313493413859894403L;

    @Id
    private String paymentId;
    @Column
    public String orderId;
}
