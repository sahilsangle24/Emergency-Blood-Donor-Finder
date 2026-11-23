
package com.ebdf.controller;

import java.io.IOException;

import com.ebdf.dao.BloodRequestDAO;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/api/blood-requests/cancel")

public class BloodRequestCancelServlet extends HttpServlet {

    private BloodRequestDAO bloodRequestDAO;
    private Gson gson;

    @Override
    public void init() {
        bloodRequestDAO = new BloodRequestDAO();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            HttpSession session = request.getSession(false);

            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(
                    "{\"message\":\"Please login first\"}"
                );
                return;
            }

            if (!"REQUESTER".equals(session.getAttribute("userRole"))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(
                    "{\"message\":\"Only requesters can cancel blood requests\"}");
                return;
            }

            Object userIdObject = session.getAttribute("userId");
            int requesterUserId = ((Number) userIdObject).intValue();

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

            boolean cancelled =
                bloodRequestDAO.cancelRequest(requestId, requesterUserId);

            if (cancelled) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(
                    "{\"message\":\"Blood request cancelled successfully\"}"
                );
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(
                    "{\"message\":\"Request not found or cannot be cancelled\"}"
                );
            }

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"message\":\"requestId must be a number\"}"
            );

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"message\":\"Unable to cancel blood request\"}"
            );
        }
    }
}
