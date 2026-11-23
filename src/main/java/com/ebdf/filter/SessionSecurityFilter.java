package com.ebdf.filter;

import com.ebdf.util.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Revalidates the account behind an existing session so deactivated users lose access promptly. */
public class SessionSecurityFilter implements Filter {
    private ServletContext servletContext;

    @Override public void init(FilterConfig config) {
        servletContext = config.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = http.getServletPath();
        if ("/api/login".equals(path) || "/api/register".equals(path)
                || "/api/logout".equals(path) || "/api/health".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = http.getSession(false);
        Object id = session == null ? null : session.getAttribute("userId");
        if (id == null) {
            chain.doFilter(request, response);
            return;
        }

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT role, account_status FROM USERS WHERE user_id=?")) {
            statement.setInt(1, ((Number) id).intValue());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !"ACTIVE".equals(result.getString("account_status"))
                        || !String.valueOf(session.getAttribute("userRole")).equals(result.getString("role"))) {
                    session.invalidate();
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    res.setContentType("application/json");
                    res.setCharacterEncoding("UTF-8");
                    res.getWriter().write("{\"message\":\"Your account is inactive or your session has expired. Please sign in again.\"}");
                    return;
                }
            }
        } catch (Exception exception) {
            servletContext.log("Session account validation failed", exception);
            res.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write("{\"message\":\"Unable to validate your session right now. Please retry.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    @Override public void destroy() { }
}
