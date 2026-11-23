package com.ebdf.controller;

import com.ebdf.util.DBConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Lightweight readiness endpoint; never returns database credentials or exception details. */
@WebServlet("/api/health")
public class HealthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM DUAL");
             ResultSet result = statement.executeQuery()) {
            if (result.next() && result.getInt(1) == 1) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"status\":\"ok\",\"database\":\"connected\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.getWriter().write("{\"status\":\"unavailable\",\"database\":\"unavailable\"}");
            }
        } catch (Exception exception) {
            getServletContext().log("Health check could not reach Oracle", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().write("{\"status\":\"unavailable\",\"database\":\"unavailable\"}");
        }
    }
}
