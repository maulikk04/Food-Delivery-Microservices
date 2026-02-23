package com.fooddelivery.restaurant_service.dto;

import com.fooddelivery.restaurant_service.model.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantResponse {
    private String id;
    private String name;
    private String address;
    private List<MenuItem> foodItems;
}
