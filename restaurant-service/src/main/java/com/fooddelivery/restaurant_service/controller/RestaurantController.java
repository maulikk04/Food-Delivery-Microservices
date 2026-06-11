package com.fooddelivery.restaurant_service.controller;

import com.fooddelivery.restaurant_service.dto.RestaurantRequest;
import com.fooddelivery.restaurant_service.dto.RestaurantResponse;
import com.fooddelivery.restaurant_service.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantResponse createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        log.info("Creating restaurant name={}", request.getName());
        RestaurantResponse response = restaurantService.createRestaurant(request);
        log.info("Created restaurant id={}", response.getId());
        return response;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RestaurantResponse> getAllRestaurants() {
        log.debug("Fetching all restaurants");
        return restaurantService.getAllRestaurants();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RestaurantResponse getRestaurantById(@PathVariable String id) {
        log.debug("Fetching restaurant id={}", id);
        return restaurantService.getRestaurantById(id);
    }
}
