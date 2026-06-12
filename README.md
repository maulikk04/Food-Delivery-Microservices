# Food Delivery Microservices

A Spring Boot Microservices-based Food Delivery Backend built to learn and implement real-world microservices concepts including Service Discovery, Centralized Configuration, API Gateway, JWT Authentication, Inter-Service Communication, Centralized Error Handling, and Structured Logging.

---

## Architecture

```text
                           ┌──────────────────────────────┐
                           │ food-delivery-config-repo    │
                           │        (GitHub Repo)         │
                           └──────────────┬───────────────┘
                                          │
                                          ▼
                           ┌──────────────────────────────┐
                           │         Config Server        │
                           └──────────────────────────────┘

                              Configuration loaded during startup by:
                               • API Gateway
                               • User Service
                               • Restaurant Service
                               • Order Service


┌──────────┐
│  Client  │
└────┬─────┘
     │
     ▼
┌──────────────────────┐
│     API Gateway      │
└──────────┬───────────┘
           │
           │ lb://SERVICE-NAME
           ▼
┌──────────────────────┐
│     Eureka Server    │
└──────────┬───────────┘
           ▲
           │
           │ Service Registration & Discovery
           │
      ┌─────────────────────┬───────────────────┬
      │                     │                   │           
      ▼                     ▼                   ▼         
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ User Service │    │ Restaurant   │    │ Order Service│
└──────┬───────┘    │   Service    │    └──────┬───────┘
       │            └──────┬───────┘           │
       ▼                   ▼                   ▼
    user_db          restaurant_db         order_db

Order Service ──Feign──► User Service
Order Service ──Feign──► Restaurant Service
- User Service, Restaurant Service, Order Service, and API Gateway register themselves with Eureka Server.
- API Gateway uses Eureka for service discovery and request routing.


┌─────────────────────────────────────────────────────────────────┐
│                    error-handling module                        │
│ Shared Exceptions • Global Handlers • Logging • Correlation IDs │
└─────────────────────────────────────────────────────────────────┘

Used by:
• API Gateway
• User Service
• Restaurant Service
• Order Service
```

---

## Services

### User Service

* User Registration
* User Login
* JWT Token Generation
* User Management

### Restaurant Service

* Add Restaurants
* View Restaurants

### Order Service

* Place Orders
* User Validation via OpenFeign
* Restaurant Validation via OpenFeign

### API Gateway

* Request Routing
* JWT Validation
* Authentication & Authorization

### Service Registry

* Eureka-based Service Discovery

### Config Server

* Centralized Configuration Management
* Loads configuration from the `food-delivery-config-repo` GitHub repository

### Error Handling Module

* Shared Exceptions
* Global Exception Handling
* Standardized Error Responses
* Correlation ID Tracking
* Structured Logging

---

## Configuration Management

Configuration is centralized using Spring Cloud Config Server.

Config Repository:

```text
https://github.com/maulikk04/food-delivery-config-repo
```

The repository contains configuration files for:

* API Gateway
* User Service
* Restaurant Service
* Order Service

### Configuration Flow

* Config Server loads configurations from `food-delivery-config-repo`.
* API Gateway fetches route configurations during startup.
* User Service, Restaurant Service, and Order Service fetch application configurations during startup.
* MongoDB connection strings are externalized using environment variables.

Example:

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
```

---

## Request Flow

### User Login Flow

1. Client sends login request to API Gateway.
2. API Gateway routes the request to User Service using Eureka Service Discovery.
3. User Service validates user credentials.
4. User Service generates a JWT token.
5. JWT token is returned to the client.
6. Client includes the token in the `Authorization` header for subsequent requests.
7. API Gateway validates the token before forwarding protected requests.

### Place Order Flow

1. Client sends order request to API Gateway.
2. API Gateway validates the JWT token.
3. API Gateway discovers Order Service through Eureka and forwards the request.
4. Order Service validates the user using User Service via OpenFeign.
5. Order Service validates the restaurant and menu items using Restaurant Service via OpenFeign.
6. Order Service creates the order and stores it in MongoDB.
7. Response is returned to the client through API Gateway.

---

## Tech Stack

* Java 17
* Spring Boot
* Spring Cloud
* MongoDB Atlas
* Spring Security
* JWT Authentication
* OpenFeign
* Eureka Server
* Spring Cloud Gateway
* Config Server
* Maven
* Lombok
* SLF4J & Logback

---

## Key Features

* Microservices Architecture
* Database per Service Pattern
* Service Discovery with Eureka
* Centralized Configuration
* API Gateway Pattern
* JWT Authentication
* OpenFeign Inter-Service Communication
* Global Exception Handling
* Correlation ID-Based Request Tracing
* Structured Logging
* DTO Pattern & Validation

---

## Project Structure

```text
food-delivery-microservices
│
├── service-registry      # Eureka Service Discovery
├── config-server         # Centralized Configuration
├── api-gateway           # Routing & JWT Validation
├── user-service          # User Management & Authentication
├── restaurant-service    # Restaurant Management
├── order-service         # Order Processing
└── error-handling        # Shared Exceptions, Logging & Correlation IDs
```

---

## API Endpoints

### User Service

| Method | Endpoint            |
| ------ | ------------------- |
| POST   | /api/users/register |
| POST   | /api/users/login    |
| GET    | /api/users/{id}     |

### Restaurant Service

| Method | Endpoint              |
| ------ | --------------------- |
| POST   | /api/restaurants      |
| GET    | /api/restaurants      |
| GET    | /api/restaurants/{id} |

### Order Service

| Method | Endpoint    |
| ------ | ----------- |
| POST   | /api/orders |

---

## Error Handling & Logging

The shared `error-handling` module provides:

* Global Exception Handling
* Standardized Error Responses
* Validation Error Handling
* Business Exceptions
* Resource Not Found Exceptions
* Unauthorized & Forbidden Exceptions
* Correlation ID Propagation (`X-Correlation-Id`)
* Structured Logging using SLF4J and Logback

---

## Getting Started

### Prerequisites

* Java 17
* Maven
* MongoDB Atlas Account
* Git

### Environment Variables

Configure the following environment variable in your IDE Run Configuration before starting the services:

```text
MONGODB_URI=<your-mongodb-atlas-uri>
```

The Config Server configuration files reference this variable using:

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
```

This keeps sensitive database credentials out of source control.

### Build Project

Before starting the services, build the entire project so that the shared `error-handling` module is available to dependent services.

```bash
mvn clean install
```

### Start Services

Run the services in the following order:

```text
1. service-registry
2. config-server
3. api-gateway
4. user-service
5. restaurant-service
6. order-service
```
