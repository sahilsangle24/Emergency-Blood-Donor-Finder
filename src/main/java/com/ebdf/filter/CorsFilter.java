package com.ebdf.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Restrictive CORS policy for optional separately-hosted development frontends. */
public class CorsFilter implements Filter {
    private Set<String> allowedOrigins;

    @Override
    public void init(FilterConfig config) {
        String configured = System.getenv("EBDF_CORS_ALLOWED_ORIGINS");
        if (configured == null || configured.isBlank()) {
            configured = "http://localhost:5173,http://127.0.0.1:5173";
        }
        allowedOrigins = Arrays.stream(configured.split(","))
                .map(String::trim).filter(value -> !value.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        res.setCharacterEncoding("UTF-8");
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        res.setHeader("Cache-Control", "no-store");

        String origin = req.getHeader("Origin");
        if (origin != null && allowedOrigins.contains(origin)) {
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Vary", "Origin");
            res.setHeader("Access-Control-Allow-Credentials", "true");
            res.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            res.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With");
            res.setHeader("Access-Control-Max-Age", "600");
        } else if ("OPTIONS".equalsIgnoreCase(req.getMethod()) && origin != null) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN, "Origin is not allowed");
            return;
        }

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            res.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        allowedOrigins = Set.of();
    }
}
