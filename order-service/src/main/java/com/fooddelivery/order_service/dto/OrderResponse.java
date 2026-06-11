package com.fooddelivery.order_service.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private String id;
    private String userId;
    private String restaurantId;
    private List<OrderItemDto> items;
    private Double totalAmount;
    private String status;
}