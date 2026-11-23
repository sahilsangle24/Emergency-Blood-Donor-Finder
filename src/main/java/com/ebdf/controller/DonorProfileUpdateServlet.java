
package com.ebdf.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Date;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ebdf.model.Donor;
import com.ebdf.service.DonorService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

// PUT handling is hosted by DonorProfileServlet to avoid duplicate servlet mappings.
public class DonorProfileUpdateServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private DonorService donorService = new DonorService();
    private Gson gson = new Gson();

    @Override
    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 1. Check login session
        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("userId") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                gson.toJson(Map.of("message", "Please login first"))
            );
            return;
        }

        // 2. Check donor role
        String role = (String) session.getAttribute("userRole");

        if (!"DONOR".equals(role)) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            response.getWriter().write(
                gson.toJson(Map.of("message", "Access denied"))
            );
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        try {
            // 3. Read JSON request
            StringBuilder jsonData = new StringBuilder();
            String line;

            try (BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    jsonData.append(line);
                }
            }

            JsonObject json = JsonParser.parseString(
                jsonData.toString()
            ).getAsJsonObject();

            // 4. Create donor object
            Donor donor = new Donor();

            donor.setUserId(userId);
            donor.setBloodGroupId(
                json.get("bloodGroupId").getAsInt()
            );

            donor.setDateOfBirth(
                Date.valueOf(
                    json.get("dateOfBirth").getAsString()
                )
            );

            donor.setAvailabilityStatus(
                json.get("availabilityStatus").getAsString()
            );

            // Optional last donation date
            if (json.has("lastDonationDate")
                    && !json.get("lastDonationDate").isJsonNull()
                    && !json.get("lastDonationDate").getAsString().isEmpty()) {

                donor.setLastDonationDate(
                    Date.valueOf(
                        json.get("lastDonationDate").getAsString()
                    )
                );
            } else {
                donor.setLastDonationDate(null);
            }

            // Contact consent
            boolean consent =
                json.has("contactConsent")
                && json.get("contactConsent").getAsBoolean();

            donor.setContactConsent(consent ? "Y" : "N");

            String phone = json.get("phone").getAsString();

            // 5. Update donor profile
            String result = donorService.updateDonorProfile(
                userId, donor, phone
            );

            if ("Donor profile updated successfully".equals(result)) {

                response.setStatus(HttpServletResponse.SC_OK);

            } else {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            response.getWriter().write(
                gson.toJson(Map.of("message", result))
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().write(
                gson.toJson(
                    Map.of("message", "Invalid profile information")
                )
            );
        }
    }
}
