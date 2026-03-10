package com.scraply.rest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component
public class AgentAuthInterceptor implements HandlerInterceptor {

    @Value("${agent.access_key}")
    private String ACCESS_KEY;

    @Value("${agent.secret_key}")
    private String SECRET_KEY;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String accessKey = request.getHeader("X-ACCESS-KEY");
        String secretKey = request.getHeader("X-SECRET-KEY");

        if (!ACCESS_KEY.equals(accessKey) || !SECRET_KEY.equals(secretKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
            errorDetails.put("error", "Unauthorized");
            errorDetails.put("message", "Invalid Agent Credentials");
            errorDetails.put("path", request.getRequestURI());

            response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
            return false;
        }

        return true;
    }
}