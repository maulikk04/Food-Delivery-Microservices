package com.fooddelivery.restaurant_service.repository;

import com.fooddelivery.restaurant_service.model.Restaurant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends MongoRepository<Restaurant,String> {
}
