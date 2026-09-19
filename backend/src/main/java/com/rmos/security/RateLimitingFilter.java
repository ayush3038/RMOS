package com.rmos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    // Track requests per IPs in memory
    private final ConcurrentHashMap<String, RequestData> requestCounts = new ConcurrentHashMap<>();

    private static class RequestData {
        int count;
        long timestamp;

        RequestData(int count, long timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }
    }

    // Limit: Max 10 requests per 10 seconds for sensitive endpoints
    private static final int MAX_REQUESTS = 10;
    private static final long TIME_WINDOW_MS = 10000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only apply to auth or expensive planning endpoints
        if (path.startsWith("/api/v1/auth") || path.startsWith("/api/v1/plans")) {
            String clientIp = request.getRemoteAddr();
            long currentTime = System.currentTimeMillis();

            requestCounts.compute(clientIp, (key, data) -> {
                if (data == null || (currentTime - data.timestamp) > TIME_WINDOW_MS) {
                    return new RequestData(1, currentTime);
                } else {
                    data.count++;
                    return data;
                }
            });

            RequestData data = requestCounts.get(clientIp);
            if (data.count > MAX_REQUESTS) {
                logger.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter()
                        .write("{\"error\":\"Too Many Requests\", \"message\":\"Please wait before trying again.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
