
package com.ebdf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ebdf.model.BloodRequest;
import com.ebdf.util.DBConnection;

public class BloodRequestDAO {

    // Create a new emergency blood request
    public boolean createRequest(BloodRequest request) {
        return createRequestWithId(request) > 0;
    }

    public int createRequestWithId(BloodRequest request) {

        String sql = "INSERT INTO BLOOD_REQUESTS "
                + "(requester_user_id, blood_group_id, location_id, "
                + "hospital_name, hospital_address, units_required, "
                + "urgency, required_by, request_details) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[] {"REQUEST_ID"})) {

            ps.setInt(1, request.getRequesterUserId());
            ps.setInt(2, request.getBloodGroupId());

            if (request.getLocationId() > 0) {
                ps.setInt(3, request.getLocationId());
            } else {
                ps.setNull(3, java.sql.Types.NUMERIC);
            }

            ps.setString(4, request.getHospitalName());
            ps.setString(5, request.getHospitalAddress());
            ps.setInt(6, request.getUnitsRequired());
            ps.setString(7, request.getUrgency());
            ps.setTimestamp(8, request.getRequiredBy());
            ps.setString(9, request.getRequestDetails());

            if (ps.executeUpdate() != 1) return -1;
            try (java.sql.ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // Cancel an open blood request
    public boolean cancelRequest(int requestId, int requesterUserId) {

        String sql = """
            UPDATE BLOOD_REQUESTS
            SET request_status = 'CANCELLED'
            WHERE request_id = ?
            AND requester_user_id = ?
            AND request_status = 'OPEN'
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, requestId);
            ps.setInt(2, requesterUserId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
