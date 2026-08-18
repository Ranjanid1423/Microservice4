package com.example.orderservice.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.orderservice.dto.UserResponse;
import com.example.orderservice.model.Order;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.core.functions.CheckedSupplier;

@Service
public class OrderService {

    private final List<Order> orders = new ArrayList<>();

    /*
     * User Service client
     */
    private final RestClient restClient;

    /*
     * Payment Service client
     */
    private final RestClient paymentRestClient;

    /*
     * Resilience4j Retry
     */
    private final Retry paymentRetry;


    public OrderService(
            RestClient.Builder restClientBuilder,
            @Value("${user.service.url}") String userServiceUrl,
            @Value("${payment.service.url}") String paymentServiceUrl) {


        /*
         * ==========================================
         * USER SERVICE
         * ==========================================
         */

        SimpleClientHttpRequestFactory userRequestFactory =
                new SimpleClientHttpRequestFactory();

        this.restClient = restClientBuilder
                .baseUrl(userServiceUrl)
                .requestFactory(userRequestFactory)
                .build();


        /*
         * ==========================================
         * PAYMENT SERVICE
         * ==========================================
         */

        SimpleClientHttpRequestFactory paymentRequestFactory =
                new SimpleClientHttpRequestFactory();

        /*
         * Connection timeout = 2 seconds
         */
        paymentRequestFactory.setConnectTimeout(
                Duration.ofSeconds(2)
        );

        /*
         * Read timeout = 3 seconds
         */
        paymentRequestFactory.setReadTimeout(
                Duration.ofSeconds(3)
        );

        this.paymentRestClient = RestClient.builder()
                .baseUrl(paymentServiceUrl)
                .requestFactory(paymentRequestFactory)
                .build();


        /*
         * ==========================================
         * RETRY CONFIGURATION
         * ==========================================
         *
         * Maximum attempts = 3
         *
         * Attempt 1
         * Attempt 2
         * Attempt 3
         *
         * Then STOP.
         */

        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .build();

        this.paymentRetry = Retry.of(
                "paymentService",
                retryConfig
        );


        /*
         * ==========================================
         * EXISTING ORDERS
         * ==========================================
         */

        orders.add(
                new Order(101L, 1L)
        );

        orders.add(
                new Order(102L, 2L)
        );

        orders.add(
                new Order(103L, 3L)
        );
    }


    /*
     * ==========================================
     * GET ORDER BY ID
     * ==========================================
     */

    public Order getOrderById(Long orderId) {

        for (Order order : orders) {

            if (order.getOrderId().equals(orderId)) {
                return order;
            }
        }

        return null;
    }


    

    public UserResponse getUserFromUserService(
            Long userId) {

        return restClient
                .get()
                .uri(
                        "/api/users/{id}",
                        userId
                )
                .retrieve()
                .body(UserResponse.class);
    }


    public String getPayment(Long paymentId) {

        return paymentRestClient
                .get()
                .uri(
                        "/api/payments/{id}",
                        paymentId
                )
                .retrieve()
                .body(String.class);
    }

    public String getSlowPayment(int seconds) {

        return paymentRestClient
                .get()
                .uri(
                        "/api/payments/slow/{seconds}",
                        seconds
                )
                .retrieve()
                .body(String.class);
    }

    public String testPaymentRetry() {

        CheckedSupplier<String> paymentCall =
                () -> paymentRestClient
                        .get()
                        .uri("/api/payments/retry-test")
                        .retrieve()
                        .body(String.class);

        CheckedSupplier<String> retryableCall =
                Retry.decorateCheckedSupplier(
                        paymentRetry,
                        paymentCall
                );

        try {

            return retryableCall.get();

        } catch (Throwable e) {

            throw new RuntimeException(
                    "Payment Service failed after "
                    + paymentRetry.getMetrics()
                            .getNumberOfFailedCallsWithRetryAttempt()
                    + " retry attempts",
                    e
            );
        }
    }


  
    public String testPaymentAlwaysFails() {

        CheckedSupplier<String> paymentCall =
                () -> paymentRestClient
                        .get()
                        .uri("/api/payments/fail")
                        .retrieve()
                        .body(String.class);

        CheckedSupplier<String> retryableCall =
                Retry.decorateCheckedSupplier(
                        paymentRetry,
                        paymentCall
                );

        try {

            return retryableCall.get();

        } catch (Throwable e) {

            throw new RuntimeException(
                    "Payment Service failed after maximum retry attempts",
                    e
            );
        }
    }
}