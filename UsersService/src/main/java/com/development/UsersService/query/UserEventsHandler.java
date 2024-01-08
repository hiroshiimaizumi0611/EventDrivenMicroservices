package com.development.UsersService.query;

import com.development.core.models.PaymentDetails;
import com.development.core.models.User;
import com.development.core.query.FetchUserPaymentDetailsQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserEventsHandler {

    @QueryHandler
    public User findUserPaymentDetails(FetchUserPaymentDetailsQuery query) {
        PaymentDetails paymentDetails = PaymentDetails.builder()
                .cardNumber("123Card")
                .cvv("123")
                .name("HIROSHI")
                .validUntilMonth(12)
                .validUntilYear(2030)
                .build();

        return User.builder()
                .firstName("Hiroshi")
                .lastName("Imaizumi")
                .userId(query.getUserId())
                .paymentDetails(paymentDetails)
                .build();
    }
}