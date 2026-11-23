
package com.ebdf.controller;

import java.io.IOException;
import java.sql.Date;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ebdf.model.Donor;
import com.ebdf.model.Location;
import com.ebdf.dao.LocationDAO;
import com.ebdf.service.DonorService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/api/donor/register")
public class DonorRegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private DonorService donorService = new DonorService();
    private Gson gson = new Gson();

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
                    "{\"message\":\"Please login first\"}");
                return;
            }

            if (!"DONOR".equals(session.getAttribute("userRole"))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(
                    "{\"message\":\"Only donors can register a donor profile\"}");
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

            Donor donor = new Donor();

            // Get user ID from session, not from client
            donor.setUserId((Integer) session.getAttribute("userId"));

            donor.setBloodGroupId(
                    json.get("bloodGroupId").getAsInt());

            if (json.has("locationId") &&
                !json.get("locationId").isJsonNull()) {
                donor.setLocationId(json.get("locationId").getAsInt());
            } else if (json.has("city") && json.has("state")
                    && json.has("pincode")) {
                String city = json.get("city").getAsString().trim();
                String state = json.get("state").getAsString().trim();
                String pincode = json.get("pincode").getAsString().trim();

                if (city.isEmpty() || state.isEmpty()
                        || !pincode.matches("[0-9]{6}")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(
                        "{\"message\":\"Enter a city, state and valid 6-digit pincode\"}");
                    return;
                }

                Location location = new Location();
                location.setAddressLine(json.has("addressLine")
                        ? json.get("addressLine").getAsString().trim() : null);
                location.setCity(city);
                location.setDistrict(json.has("district")
                        ? json.get("district").getAsString().trim() : null);
                location.setState(state);
                location.setPincode(pincode);

                int locationId = new LocationDAO().addLocation(location);
                if (locationId <= 0) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write(
                        "{\"message\":\"Unable to save donor location\"}");
                    return;
                }
                donor.setLocationId(locationId);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(
                    "{\"message\":\"Donor city, state and pincode are required\"}");
                return;
            }

            donor.setDateOfBirth(
                    Date.valueOf(json.get("dateOfBirth").getAsString()));

            donor.setAvailabilityStatus(
                    json.get("availabilityStatus").getAsString());

            if (json.has("lastDonationDate") &&
                !json.get("lastDonationDate").isJsonNull() &&
                !json.get("lastDonationDate").getAsString().isEmpty()) {

                donor.setLastDonationDate(
                    Date.valueOf(json.get("lastDonationDate").getAsString()));
            }

            donor.setContactConsent(
            	    json.has("contactConsent") &&
            	    json.get("contactConsent").getAsBoolean() ? "Y" : "N");

            String result = donorService.registerDonor(donor);

            JsonObject output = new JsonObject();
            output.addProperty("message", result);

            if ("Donor registration successful".equals(result)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            response.getWriter().write(gson.toJson(output));

        } catch (Exception e) {
            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"message\":\"Invalid donor registration details\"}");
        }
    }
}
