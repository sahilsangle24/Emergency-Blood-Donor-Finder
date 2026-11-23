
package com.ebdf.controller;

import java.io.IOException;
import java.util.Map;

import com.ebdf.dao.BloodRequestDetailsDAO;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/blood-requests/details")

public class BloodRequestDetailsServlet extends HttpServlet {

    private BloodRequestDetailsDAO bloodRequestDetailsDAO;
    private Gson gson;

    @Override
    public void init() {
        bloodRequestDetailsDAO = new BloodRequestDetailsDAO();
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

            String requestIdParam = request.getParameter("requestId");

            if (requestIdParam == null || requestIdParam.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"message\":\"requestId is required\"}"
                );
                return;
            }

            int requestId = Integer.parseInt(requestIdParam);

            if (requestId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"message\":\"Invalid requestId\"}"
                );
                return;
            }

            Map<String, Object> bloodRequest =
                bloodRequestDetailsDAO.getRequestById(requestId);

            if (bloodRequest == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(
                    "{\"message\":\"Blood request not found\"}"
                );
                return;
            }
            String role = String.valueOf(session.getAttribute("userRole"));
            int userId = ((Number) session.getAttribute("userId")).intValue();
            int ownerId = ((Number) bloodRequest.get("requesterUserId")).intValue();
            boolean allowed = "ADMIN".equals(role)
                    || ("REQUESTER".equals(role) && ownerId == userId)
                    || ("DONOR".equals(role) && bloodRequestDetailsDAO.canDonorViewRequest(requestId, userId));
            if (!allowed) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"message\":\"Access denied. Donors can view only compatible requests available to them or requests they were invited to.\"}");
                return;
            }

            if ("DONOR".equals(role)) {
                // Older records may contain a patient name in this free-text field.
                bloodRequest.remove("requestDetails");
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(bloodRequest));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"message\":\"requestId must be a number\"}"
            );

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"message\":\"Unable to fetch blood request details\"}"
            );
        }
    }
}
