
package com.ebdf.controller;

import java.io.IOException;
import java.util.List;

import com.ebdf.dao.BloodRequestSearchDAO;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/blood-requests")

public class BloodRequestSearchServlet extends HttpServlet {

    private BloodRequestSearchDAO bloodRequestSearchDAO;
    private Gson gson;

    @Override
    public void init() {
        bloodRequestSearchDAO = new BloodRequestSearchDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"message\":\"Please login first\"}");
                return;
            }
            String role = String.valueOf(session.getAttribute("userRole"));
            if (!"DONOR".equals(role) && !"ADMIN".equals(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"message\":\"Only donors can view open blood requests\"}");
                return;
            }

            List<java.util.Map<String,Object>> requests =
                bloodRequestSearchDAO.getOpenBloodRequests(((Number) session.getAttribute("userId")).intValue());

            response.setStatus(HttpServletResponse.SC_OK);

            response.getWriter().write(
                gson.toJson(requests)
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                "{\"message\":\"Unable to fetch blood requests\"}"
            );
        }
    }
}
