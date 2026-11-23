
package com.ebdf.controller;

import java.io.IOException;
import java.sql.Timestamp;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ebdf.model.BloodRequest;
import com.ebdf.service.BloodRequestService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/api/blood-request")
public class BloodRequestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private BloodRequestService requestService = new BloodRequestService();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            if (!new com.ebdf.dao.AppSettingsDAO().enabled("allowBloodRequests", true)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"message\":\"Blood request submissions are temporarily disabled\"}");
                return;
            }
            HttpSession session = request.getSession(false);

            if (session == null ||
                session.getAttribute("userId") == null) {

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(
                    "{\"message\":\"Please login first\"}");
                return;
            }

            if (!"REQUESTER".equals(session.getAttribute("userRole"))) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(
                    "{\"message\":\"Only requesters can create blood requests\"}");
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

            BloodRequest bloodRequest = new BloodRequest();

            // Get requester ID from session
            bloodRequest.setRequesterUserId(
                (Integer) session.getAttribute("userId"));

            bloodRequest.setBloodGroupId(
                json.get("bloodGroupId").getAsInt());

            if (json.has("locationId") &&
                !json.get("locationId").isJsonNull()) {

                bloodRequest.setLocationId(
                    json.get("locationId").getAsInt());
            }

            bloodRequest.setHospitalName(
                json.get("hospitalName").getAsString());

            bloodRequest.setHospitalAddress(
                json.get("hospitalAddress").getAsString());

            bloodRequest.setUnitsRequired(
                json.get("unitsRequired").getAsInt());

            bloodRequest.setUrgency(
                json.get("urgency").getAsString());

            String requiredBy = json.get("requiredBy").getAsString();

            bloodRequest.setRequiredBy(
                Timestamp.valueOf(requiredBy.replace("T", " ") + (requiredBy.length() == 16 ? ":00" : "")));

            if (json.has("requestDetails") &&
                !json.get("requestDetails").isJsonNull()) {

                bloodRequest.setRequestDetails(
                    json.get("requestDetails").getAsString());
            }

            int requestId = requestService.createRequestWithId(bloodRequest);
            JsonObject output = new JsonObject();
            if (requestId > 0) {
                output.addProperty("message", "Blood request created successfully");
                output.addProperty("requestId", requestId);
                notifyCompatibleDonors(requestId, bloodRequest.getBloodGroupId());
                response.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                output.addProperty("message", "Blood request creation failed. Verify the entered details.");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            response.getWriter().write(gson.toJson(output));

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"message\":\"Invalid blood request details\"}");
        }
    }

    private void notifyCompatibleDonors(int requestId, int recipientGroupId) {
        if (!new com.ebdf.dao.AppSettingsDAO().enabled("emergencyAlerts", true)) return;
        String sql = "SELECT DISTINCT d.user_id FROM DONORS d JOIN USERS u ON u.user_id=d.user_id "
                + "JOIN BLOOD_COMPATIBILITY bc ON bc.donor_group_id=d.blood_group_id "
                + "WHERE bc.recipient_group_id=? AND d.availability_status='AVAILABLE' "
                + "AND d.contact_consent='Y' AND u.account_status='ACTIVE'";
        try (java.sql.Connection c=com.ebdf.util.DBConnection.getConnection();
             java.sql.PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setInt(1,recipientGroupId);
            try (java.sql.ResultSet rs=ps.executeQuery()) {
                while(rs.next()) com.ebdf.dao.PortalDAO.createNotification(rs.getInt(1),
                    "New compatible blood request", "A new emergency request is available (#"+requestId+")", "REQUEST", requestId);
            }
        } catch(Exception e) { getServletContext().log("Unable to notify matching donors",e); }
    }
}
