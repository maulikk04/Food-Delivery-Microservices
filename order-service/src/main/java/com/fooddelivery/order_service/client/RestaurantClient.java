package com.fooddelivery.order_service.client;

import com.fooddelivery.order_service.dto.Restaurant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurant-service")
public interface RestaurantClient {

    @GetMapping("/api/restaurants/{id}")
    Restaurant getRestaurantById(@PathVariable("id") String id);
}
