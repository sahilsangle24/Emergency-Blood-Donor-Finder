
package com.ebdf.controller;

import java.io.IOException;
import java.sql.Date;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ebdf.dao.DonorDAO;
import com.ebdf.dao.PortalDAO;
import com.ebdf.model.DonationHistory;
import com.ebdf.service.DonationHistoryService;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/api/donation-history")

public class DonationHistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private DonationHistoryService historyService =
            new DonationHistoryService();

    private DonorDAO donorDAO = new DonorDAO();

    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\":\"Please login first\"}");
            return;
        }
        if (!"DONOR".equals(session.getAttribute("userRole"))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"message\":\"Only donors can view donation history\"}");
            return;
        }
        try {
            response.getWriter().write(gson.toJson(new PortalDAO().donationHistory(((Number) session.getAttribute("userId")).intValue())));
        } catch (Exception e) {
            getServletContext().log("Donation history retrieval failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"Unable to load donation history\"}");
        }
    }

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
                    "{\"message\":\"Only donors can add donation history\"}");
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

            // Find donor ID using logged-in user ID
            int userId = (Integer) session.getAttribute("userId");

            int donorId = donorDAO.findDonorIdByUserId(userId);

            if (donorId <= 0) {

                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(
                    "{\"message\":\"Donor profile not found\"}");
                return;
            }

            DonationHistory history = new DonationHistory();

            history.setDonorId(donorId);

            // Optional blood request ID
            if (json.has("requestId") &&
                !json.get("requestId").isJsonNull()) {

                history.setRequestId(
                    json.get("requestId").getAsInt());
            }

            history.setDonationDate(
                Date.valueOf(json.get("donationDate").getAsString()));

            history.setUnitsDonated(
                json.get("unitsDonated").getAsInt());

            history.setHospitalName(
                json.get("hospitalName").getAsString());

            // New donation records require verification
            history.setVerificationStatus("PENDING");

            String result = historyService.addDonationHistory(history);

            JsonObject output = new JsonObject();
            output.addProperty("message", result);
            if ("Donation history added successfully".equals(result)) {
                PortalDAO.notifyAdmins("Donation verification required", "A donor submitted a new donation record for review.", "DONATION_REVIEW");
            }

            if ("Donation history added successfully".equals(result)) {

                response.setStatus(HttpServletResponse.SC_CREATED);

            } else {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            response.getWriter().write(gson.toJson(output));

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"message\":\"Invalid donation history details\"}");
        }
    }
}
