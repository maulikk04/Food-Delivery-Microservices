package com.fooddelivery.api_gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddelivery.common.error.ErrorResponse;
import com.fooddelivery.common.exception.ForbiddenException;
import com.fooddelivery.common.exception.UnauthorizedException;
import com.fooddelivery.common.web.CorrelationIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler implements WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);
    private final ObjectMapper objectMapper;

    public GlobalErrorWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = resolveStatus(ex);
        String message = resolveMessage(ex, status);

        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, exchange.getRequest().getPath().value());

        String correlationId = exchange.getRequest().getHeaders().getFirst(CorrelationIdUtil.CORRELATION_ID_HEADER);
        if (correlationId != null) {
            MDC.put("correlationId", correlationId);
        }

        try {
            if (status.is4xxClientError()) {
                log.warn("Gateway error status={} path={}", status.value(), exchange.getRequest().getPath());
            } else {
                log.error("Gateway error status={} path={}", status.value(), exchange.getRequest().getPath(), ex);
            }

            byte[] bytes = objectMapper.writeValueAsBytes(body);
            response.setStatusCode(status);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } catch (JsonProcessingException e) {
            byte[] bytes = "{\"message\":\"Internal server error\"}".getBytes(StandardCharsets.UTF_8);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
        } finally {
            MDC.remove("correlationId");
        }
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            return HttpStatus.valueOf(rse.getStatusCode().value());
        }
        if (ex instanceof UnauthorizedException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (ex instanceof ForbiddenException) {
            return HttpStatus.FORBIDDEN;
        }
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(Throwable ex, HttpStatus status) {
        if (ex instanceof ResponseStatusException) {
            ResponseStatusException rse = (ResponseStatusException) ex;
            return rse.getReason() != null ? rse.getReason() : status.getReasonPhrase();
        }
        if (status.is5xxServerError()) {
            return "Internal server error";
        }
        return ex.getMessage() != null ? ex.getMessage() : status.getReasonPhrase();
    }
}
