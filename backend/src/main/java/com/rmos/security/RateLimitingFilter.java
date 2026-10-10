package com.rmos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Value("${rmos.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${rmos.rate-limit.max-requests:10}")
    private int maxRequests;

    @Value("${rmos.rate-limit.time-window-ms:10000}")
    private long timeWindowMs;

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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // Only apply to auth, expensive planning, or LLM chat endpoints
        if (path.startsWith("/api/v1/auth") || path.startsWith("/api/v1/plans")
                || path.startsWith("/api/v1/chat")) {
            String clientIp = request.getRemoteAddr();
            long currentTime = System.currentTimeMillis();

            requestCounts.compute(clientIp, (key, data) -> {
                if (data == null || (currentTime - data.timestamp) > timeWindowMs) {
                    return new RequestData(1, currentTime);
                } else {
                    data.count++;
                    return data;
                }
            });

            RequestData data = requestCounts.get(clientIp);
            if (data.count > maxRequests) {
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
