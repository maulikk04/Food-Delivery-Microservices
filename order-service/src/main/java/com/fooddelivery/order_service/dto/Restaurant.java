package com.fooddelivery.order_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class Restaurant {
    private String id;
    private String name;
    private List<MenuItemDto> foodItems;
}
