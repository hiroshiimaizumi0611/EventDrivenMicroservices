package com.development.core.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentProcessedEvent {
    private final String orderId;
    private final String paymentId;
}
