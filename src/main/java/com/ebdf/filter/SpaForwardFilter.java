package com.ebdf.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/** Forwards extensionless React Router paths to the packaged SPA entry point. */
public class SpaForwardFilter implements Filter {
    @Override public void init(FilterConfig config) { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request;
        String path = http.getRequestURI().substring(http.getContextPath().length());
        if (path.startsWith("/app/") && !path.substring("/app/".length()).contains(".")) {
            request.getRequestDispatcher("/app/index.html").forward(request, response);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override public void destroy() { }
}
