package com.example.orderservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.UserResponse;
import com.example.orderservice.model.Order;
import com.example.orderservice.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /*
     * =====================================================
     * EXISTING ORDER ENDPOINT
     * =====================================================
     */

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long id) {

        // Step 1: Find the order
        Order order = orderService.getOrderById(id);

        // Step 2: Order doesn't exist
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        // Step 3: Get user ID from order
        Long userId = order.getUserId();

        // Step 4: Get user information from User Service
        UserResponse user =
                orderService.getUserFromUserService(userId);

        // Step 5: Create intentional API response
        OrderResponse response =
                new OrderResponse(
                        order.getOrderId(),
                        order.getUserId(),
                        user
                );

        // Step 6: Return response
        return ResponseEntity.ok(response);
    }


    /*
     * =====================================================
     * DAY 4 - NORMAL PAYMENT CALL
     * =====================================================
     *
     * Example:
     *
     * GET /api/orders/101/payment/500
     *
     */

    @GetMapping("/{orderId}/payment/{paymentId}")
    public ResponseEntity<String> getPayment(
            @PathVariable Long orderId,
            @PathVariable Long paymentId) {

        try {

            String paymentResponse =
                    orderService.getPayment(paymentId);

            return ResponseEntity.ok(
                    "Order " + orderId
                    + " -> "
                    + paymentResponse
            );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Payment Service call failed: "
                            + e.getClass().getSimpleName()
                    );
        }
    }


    /*
     * =====================================================
     * DAY 4 - SLOW PAYMENT / TIMEOUT TEST
     * =====================================================
     *
     * Example:
     *
     * GET /api/orders/101/slow-payment/10
     *
     * Payment Service will wait 10 seconds.
     *
     * Order Service has a 3-second read timeout.
     *
     */

    @GetMapping("/{orderId}/slow-payment/{seconds}")
    public ResponseEntity<String> getSlowPayment(
            @PathVariable Long orderId,
            @PathVariable int seconds) {

        try {

            String paymentResponse =
                    orderService.getSlowPayment(seconds);

            return ResponseEntity.ok(
                    "Order " + orderId
                    + " -> "
                    + paymentResponse
            );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Payment Service timed out or failed: "
                            + e.getClass().getSimpleName()
                    );
        }
    }
}