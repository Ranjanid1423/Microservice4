package com.example.orderservice.dto;

public class OrderResponse {

    private Long orderId;
    private Long userId;
    private UserResponse user;

    public OrderResponse() {
    }

    public OrderResponse(Long orderId, Long userId, UserResponse user) {
        this.orderId = orderId;
        this.userId = userId;
        this.user = user;
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

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}