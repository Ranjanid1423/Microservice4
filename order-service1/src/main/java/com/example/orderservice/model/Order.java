package com.example.orderservice.model;

public class Order {

    private Long orderId;
    private Long userId;

    public Order() {
    }

    public Order(Long orderId, Long userId) {
        this.orderId = orderId;
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}