package com.fooddelivery.api_gateway.filter;

import com.fooddelivery.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {
                // Check if header contains token
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                                    log.warn("Missing authorization header path={}", exchange.getRequest().getPath());
                                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authorization header");
                                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    jwtUtil.validateToken(authHeader);
                    Claims claims = jwtUtil.getClaims(authHeader);
                    String userId = claims.get("userId", String.class);
                    String role = claims.get("role", String.class);

                    exchange = exchange.mutate().request(
                            exchange.getRequest().mutate().header("X-Auth-User-Id",userId).header("X-Auth-User-Role",role).build()
                    ).build();
                } catch (Exception e) {
                                    log.warn("Unauthorized access path={}", exchange.getRequest().getPath(), e);
                                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized access to application");
                                }
            }
            return chain.filter(exchange);
        });
    }

    public static class Config {
    }
}
