package com.fooddelivery.restaurant_service.config;

import com.fooddelivery.common.exception.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class HeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String role = request.getHeader("X-Auth-User-Role");
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (path.equals("/api/restaurants") && method.equals("POST")) {
            if (role == null || !role.equals("ROLE_OWNER")) {
                throw new ForbiddenException("Access Denied: Only Owners can add restaurants.");
            }
        }
        return true;
    }
}