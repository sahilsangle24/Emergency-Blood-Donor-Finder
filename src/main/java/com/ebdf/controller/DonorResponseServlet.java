
package com.ebdf.controller;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ebdf.dao.DonorDAO;
import com.ebdf.service.DonorResponseService;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/api/donor/respond")

public class DonorResponseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private DonorResponseService responseService =
            new DonorResponseService();

    private DonorDAO donorDAO = new DonorDAO();

    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            HttpSession session = request.getSession(false);

            if (session == null ||
                session.getAttribute("userId") == null) {

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(
                    "{\"message\":\"Please login first\"}");
                return;
            }

            if (!"DONOR".equals(session.getAttribute("userRole"))) {

                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(
                    "{\"message\":\"Only donors can respond\"}");
                return;
            }

            JsonObject json = gson.fromJson(
                    request.getReader(), JsonObject.class);

            if (json == null) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"message\":\"Invalid request body\"}");
                return;
            }

            int requestId = json.get("requestId").getAsInt();

            String responseStatus =
                    json.get("responseStatus").getAsString();

            String responseNotes = null;

            if (json.has("responseNotes") &&
                !json.get("responseNotes").isJsonNull()) {

                responseNotes =
                        json.get("responseNotes").getAsString();
            }

            // Get logged-in user ID
            int userId = (Integer) session.getAttribute("userId");

            // Find donor ID using user ID
            int donorId = donorDAO.findDonorIdByUserId(userId);

            if (donorId <= 0) {

                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(
                    "{\"message\":\"Donor profile not found. Please register as a donor.\"}");
                return;
            }

            // Submit donor response
            String result = responseService.respondToRequest(
                    requestId,
                    donorId,
                    responseStatus,
                    responseNotes
            );

            JsonObject output = new JsonObject();
            output.addProperty("message", result);

            if ("Response submitted successfully".equals(result)) {

                response.setStatus(HttpServletResponse.SC_OK);

            } else {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            response.getWriter().write(gson.toJson(output));

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                "{\"message\":\"Invalid donor response details\"}");
        }
    }
}
