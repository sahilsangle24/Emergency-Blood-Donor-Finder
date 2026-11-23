
package com.ebdf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.ebdf.model.DonationHistory;
import com.ebdf.util.DBConnection;

public class DonationHistoryDAO {

    // Save completed donation history
    public boolean addDonationHistory(DonationHistory history) {

        String sql = "INSERT INTO DONATION_HISTORY "
                + "(donor_id, request_id, donation_date, "
                + "units_donated, verification_status, hospital_name) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            if (history.getRequestId() > 0) {
                String ownershipSql = "SELECT COUNT(*) FROM DONOR_RESPONSES WHERE REQUEST_ID=? AND DONOR_ID=? AND RESPONSE_STATUS='ACCEPTED'";
                try (PreparedStatement ownership = con.prepareStatement(ownershipSql)) {
                    ownership.setInt(1, history.getRequestId());
                    ownership.setInt(2, history.getDonorId());
                    try (java.sql.ResultSet result = ownership.executeQuery()) {
                        if (!result.next() || result.getInt(1) != 1) {
                            con.rollback();
                            return false;
                        }
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, history.getDonorId());

            if (history.getRequestId() > 0) {
                ps.setInt(2, history.getRequestId());
            } else {
                ps.setNull(2, java.sql.Types.NUMERIC);
            }

            ps.setDate(3, history.getDonationDate());
            ps.setInt(4, history.getUnitsDonated());
            ps.setString(5, history.getVerificationStatus());
            ps.setString(6, history.getHospitalName());

            boolean inserted = ps.executeUpdate() == 1;
            if (inserted) con.commit(); else con.rollback();
            return inserted;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
