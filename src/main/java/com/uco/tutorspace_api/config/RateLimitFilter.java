package com.uco.tutorspace_api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 20;
    private static final long WINDOW_SECONDS = 60;

    private final Map<String, RequestBucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (!request.getRequestURI().startsWith("/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        RequestBucket bucket = buckets.computeIfAbsent(ip, k -> new RequestBucket());

        if (bucket.tryConsume()) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"Demasiadas peticiones. Intenta de nuevo en 1 minuto\"}"
            );
            response.getWriter().flush();
        }
    }

    private static class RequestBucket {
        private int count = 0;
        private long windowStart = Instant.now().getEpochSecond();

        synchronized boolean tryConsume() {
            long now = Instant.now().getEpochSecond();
            
            if (now - windowStart >= WINDOW_SECONDS) {
                count = 0;
                windowStart = now;
            }
            
            if (count < MAX_REQUESTS) {
                count++;
                return true;
            }
            return false;
        }
    }
}
