package com.fooddelivery.order_service.service;

import com.fooddelivery.common.exception.BusinessException;
import com.fooddelivery.common.exception.ResourceNotFoundException;
import com.fooddelivery.order_service.client.RestaurantClient;
import com.fooddelivery.order_service.client.UserClient;
import com.fooddelivery.order_service.dto.*;
import com.fooddelivery.order_service.model.Order;
import com.fooddelivery.order_service.model.OrderItem;
import com.fooddelivery.order_service.repository.OrderRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final RestaurantClient restaurantClient;

    public OrderResponse placeOrder(OrderRequest request, String authenticatedUserId) {
        try {
            userClient.getUserById(authenticatedUserId);
        } catch (FeignException e) {
            if (e.status() == 404) {
                log.warn("User not found userId={}", authenticatedUserId, e);
                throw new ResourceNotFoundException("User not found with id: " + authenticatedUserId);
            }
            log.warn("User service unavailable userId={}", authenticatedUserId, e);
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "User service unavailable");
        }

        Restaurant restaurant;
        try {
            restaurant = restaurantClient.getRestaurantById(request.getRestaurantId());
        } catch (FeignException e) {
            if (e.status() == 404) {
                log.warn("Restaurant not found restaurantId={}", request.getRestaurantId(), e);
                throw new ResourceNotFoundException("Restaurant not found with id: " + request.getRestaurantId());
            }
            log.warn("Restaurant service unavailable restaurantId={}", request.getRestaurantId(), e);
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Restaurant service unavailable");
        }

        double total = 0.0;

        for (OrderItemDto requestedItem : request.getItems()) {
            MenuItemDto realMenuItem = restaurant.getFoodItems().stream()
                    .filter(menuItem -> menuItem.getName().equalsIgnoreCase(requestedItem.getName()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found on menu: " + requestedItem.getName()));
            requestedItem.setPrice(realMenuItem.getPrice());
            total += (realMenuItem.getPrice() * requestedItem.getQuantity());
        }
        Order order = Order.builder()
                .userId(authenticatedUserId)
                .restaurantId(request.getRestaurantId())
                .items(mapToOrderItems(request.getItems()))
                .totalAmount(total)
                .status("PENDING")
                .build();

        Order savedOrder = orderRepository.save(order);
        return mapToOrderResponse(savedOrder);
    }

    private List<OrderItem> mapToOrderItems(List<OrderItemDto> dtos) {
        return dtos.stream().map(dto -> OrderItem.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .build()).collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream().map(item -> {
            OrderItemDto dto = new OrderItemDto();
            dto.setName(item.getName());
            dto.setPrice(item.getPrice());
            dto.setQuantity(item.getQuantity());
            return dto;
        }).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .restaurantId(order.getRestaurantId())
                .items(itemDtos)
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .build();
    }
}
