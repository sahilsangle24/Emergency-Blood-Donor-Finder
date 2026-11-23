
package com.ebdf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ebdf.util.DBConnection;

public class BloodRequestDetailsDAO {

    public Map<String, Object> getRequestById(int requestId)
            throws Exception {

        Map<String, Object> request = null;

        String sql = """
            SELECT
                br.request_id,
                br.requester_user_id,
                u.full_name AS requester_name,
                bg.blood_group_code,
                br.hospital_name,
                br.hospital_address,
                br.units_required,
                br.urgency,
                br.required_by,
                br.request_details,
                br.request_status,
                br.created_at
            FROM BLOOD_REQUESTS br
            JOIN USERS u
                ON br.requester_user_id = u.user_id
            JOIN BLOOD_GROUPS bg
                ON br.blood_group_id = bg.blood_group_id
            WHERE br.request_id = ?
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, requestId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    request = new LinkedHashMap<>();

                    request.put("requestId", rs.getInt("request_id"));
                    request.put("requesterUserId", rs.getInt("requester_user_id"));
                    request.put("requesterName", rs.getString("requester_name"));
                    request.put("bloodGroup", rs.getString("blood_group_code"));
                    request.put("hospitalName", rs.getString("hospital_name"));
                    request.put("hospitalAddress", rs.getString("hospital_address"));
                    request.put("unitsRequired", rs.getInt("units_required"));
                    request.put("urgency", rs.getString("urgency"));
                    request.put("requiredBy", rs.getTimestamp("required_by") == null ? null : rs.getTimestamp("required_by").toString());
                    request.put("requestDetails", rs.getString("request_details"));
                    request.put("requestStatus", rs.getString("request_status"));
                    request.put("createdAt", rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toString());
                }
            }
        }

        return request;
    }

    public boolean canDonorViewRequest(int requestId, int donorUserId) throws Exception {
        String sql = """
            SELECT COUNT(*)
            FROM BLOOD_REQUESTS br
            JOIN DONORS d ON d.user_id = ?
            JOIN USERS donor_user ON donor_user.user_id = d.user_id
            JOIN BLOOD_COMPATIBILITY bc
              ON bc.donor_group_id = d.blood_group_id
             AND bc.recipient_group_id = br.blood_group_id
            WHERE br.request_id = ?
              AND donor_user.account_status = 'ACTIVE'
              AND (
                    (br.request_status = 'OPEN' AND d.availability_status = 'AVAILABLE' AND d.contact_consent = 'Y')
                    OR EXISTS (SELECT 1 FROM DONOR_RESPONSES dr WHERE dr.request_id = br.request_id AND dr.donor_id = d.donor_id)
                    OR EXISTS (SELECT 1 FROM DONOR_INVITATIONS di WHERE di.request_id = br.request_id AND di.donor_id = d.donor_id)
                  )
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, donorUserId);
            ps.setInt(2, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
