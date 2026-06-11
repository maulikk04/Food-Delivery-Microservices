package com.fooddelivery.order_service.controller;

import com.fooddelivery.common.exception.ForbiddenException;
import com.fooddelivery.order_service.dto.OrderRequest;
import com.fooddelivery.order_service.dto.OrderResponse;
import com.fooddelivery.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse placeOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader("X-Auth-User-Id") String userId,
            @RequestHeader("X-Auth-User-Role") String role
    ) {
        log.info("Placing order userId={} restaurantId={}", userId, request.getRestaurantId());
        if (!"ROLE_USER".equalsIgnoreCase(role)) {
            throw new ForbiddenException("Access Denied: Only customers can place orders.");
        }
        OrderResponse response = orderService.placeOrder(request, userId);
        log.info("Order placed userId={} orderId={}", userId, response.getId());
        return response;
    }
}