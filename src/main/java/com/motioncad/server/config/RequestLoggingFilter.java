package com.motioncad.server.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@Profile({ "dev", "local", "default" }) // 개발 환경에서만 활성화
public class RequestLoggingFilter implements Filter {

    private static final int MAX_PAYLOAD_LENGTH = 1000; // 최대 1000자까지만 출력

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Swagger UI 관련 요청은 로깅 제외
        String uri = httpRequest.getRequestURI();
        if (uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs")) {
            chain.doFilter(request, response);
            return;
        }

        // 대용량 파일 업로드는 로깅 제외 (성능 영향 방지)
        String contentType = httpRequest.getContentType();
        if (contentType != null && contentType.startsWith("multipart/form-data")) {
            log.debug("Skipping logging for multipart request: {} {}", httpRequest.getMethod(), uri);
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(wrappedRequest, wrappedResponse, duration);
            wrappedResponse.copyBodyToResponse(); // 중요: 응답 바디를 실제 응답으로 복사
        }
    }

    private void logRequest(ContentCachingRequestWrapper request,
            ContentCachingResponseWrapper response,
            long duration) {
        StringBuilder logMessage = new StringBuilder("\n");
        logMessage.append("┌─────────────────────────────────────────────────────────────────\n");
        logMessage.append(String.format("│ HTTP Request: %s %s\n",
                request.getMethod(), request.getRequestURI()));

        // Query String
        if (request.getQueryString() != null) {
            logMessage.append(String.format("│ Query: %s\n", request.getQueryString()));
        }

        // Headers
        Map<String, String> headers = getHeaders(request);
        if (!headers.isEmpty()) {
            logMessage.append("│ Headers:\n");
            headers.forEach((key, value) -> {
                // 민감한 헤더는 마스킹
                if (key.toLowerCase().contains("authorization") ||
                        key.toLowerCase().contains("token")) {
                    value = "***MASKED***";
                }
                logMessage.append(String.format("│   %s: %s\n", key, value));
            });
        }

        // Request Body
        String requestBody = getRequestBody(request);
        if (requestBody != null && !requestBody.isEmpty()) {
            logMessage.append("│ Request Body:\n");
            logMessage.append(formatPayload(requestBody, "│   "));
        }

        logMessage.append("├─────────────────────────────────────────────────────────────────\n");
        logMessage.append(String.format("│ Response Status: %d\n", response.getStatus()));
        logMessage.append(String.format("│ Duration: %d ms\n", duration));

        // Response Body (작은 응답만)
        String responseBody = getResponseBody(response);
        if (responseBody != null && !responseBody.isEmpty() && responseBody.length() < MAX_PAYLOAD_LENGTH) {
            logMessage.append("│ Response Body:\n");
            logMessage.append(formatPayload(responseBody, "│   "));
        } else if (responseBody != null && responseBody.length() >= MAX_PAYLOAD_LENGTH) {
            logMessage.append(String.format("│ Response Body: [TOO LARGE - %d bytes]\n", responseBody.length()));
        }

        logMessage.append("└─────────────────────────────────────────────────────────────────");

        log.info(logMessage.toString());
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        return headers;
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String formatPayload(String payload, String prefix) {
        if (payload.length() > MAX_PAYLOAD_LENGTH) {
            payload = payload.substring(0, MAX_PAYLOAD_LENGTH) + "... [TRUNCATED]";
        }

        // JSON인 경우 줄바꿈 유지
        String[] lines = payload.split("\n");
        StringBuilder formatted = new StringBuilder();
        for (String line : lines) {
            formatted.append(prefix).append(line).append("\n");
        }
        return formatted.toString();
    }
}
