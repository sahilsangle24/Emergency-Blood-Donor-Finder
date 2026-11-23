
package com.ebdf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ebdf.model.Donor;
import com.ebdf.util.DBConnection;

public class DonorDAO {

    // 1. Register donor profile
    public boolean registerDonor(Donor donor) {

        String sql = "INSERT INTO DONORS "
                + "(user_id, blood_group_id, location_id, date_of_birth, "
                + "availability_status, contact_consent) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, donor.getUserId());
            ps.setInt(2, donor.getBloodGroupId());

            if (donor.getLocationId() > 0) {
                ps.setInt(3, donor.getLocationId());
            } else {
                ps.setNull(3, java.sql.Types.NUMERIC);
            }

            ps.setDate(4, donor.getDateOfBirth());
            ps.setString(5, donor.getAvailabilityStatus());
            ps.setString(6, donor.getContactConsent());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 2. Find donor ID using registered user ID
    public int findDonorIdByUserId(int userId) {

        String sql = "SELECT donor_id FROM DONORS WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("donor_id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // 3. Get complete donor profile using user ID
    public Map<String, Object> getDonorProfileByUserId(int userId) {

        String sql =
                "SELECT u.user_id, u.full_name, u.email, u.phone, "
              + "d.donor_id, d.blood_group_id, d.date_of_birth, "
              + "d.availability_status, d.last_donation_date, "
              + "d.contact_consent, bg.blood_group_code, "
              + "l.city, l.district, l.state, l.pincode "
              + "FROM USERS u "
              + "JOIN DONORS d ON u.user_id = d.user_id "
              + "JOIN BLOOD_GROUPS bg "
              + "ON d.blood_group_id = bg.blood_group_id "
              + "LEFT JOIN LOCATIONS l "
              + "ON d.location_id = l.location_id "
              + "WHERE u.user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    Map<String, Object> profile = new LinkedHashMap<>();

                    profile.put("userId", rs.getInt("user_id"));
                    profile.put("donorId", rs.getInt("donor_id"));
                    profile.put("fullName", rs.getString("full_name"));
                    profile.put("email", rs.getString("email"));
                    profile.put("phone", rs.getString("phone"));

                    profile.put("bloodGroupId",
                            rs.getInt("blood_group_id"));

                    profile.put("bloodGroup",
                            rs.getString("blood_group_code"));

                    java.sql.Date dob = rs.getDate("date_of_birth");

                    profile.put("dateOfBirth",
                            dob != null ? dob.toString() : null);

                    profile.put("availabilityStatus",
                            rs.getString("availability_status"));

                    java.sql.Date lastDonation =
                            rs.getDate("last_donation_date");

                    profile.put("lastDonationDate",
                            lastDonation != null
                                    ? lastDonation.toString()
                                    : null);

                    profile.put("contactConsent",
                            rs.getString("contact_consent"));

                    profile.put("city", rs.getString("city"));
                    profile.put("district", rs.getString("district"));
                    profile.put("state", rs.getString("state"));
                    profile.put("pincode", rs.getString("pincode"));

                    return profile;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // 4. Update donor profile
    public boolean updateDonorProfile(int userId, Donor donor, String phone) {

        String userSql =
                "UPDATE USERS SET phone = ? WHERE user_id = ?";

        String donorSql =
                "UPDATE DONORS SET blood_group_id = ?, "
              + "date_of_birth = ?, availability_status = ?, "
              + "last_donation_date = ?, contact_consent = ?, "
              + "location_id = COALESCE(?, location_id) "
              + "WHERE user_id = ?";

        Connection con = null;

        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Update USERS table
            try (PreparedStatement ps = con.prepareStatement(userSql)) {

                ps.setString(1, phone);
                ps.setInt(2, userId);

                if (ps.executeUpdate() != 1) {
                    con.rollback();
                    return false;
                }
            }

            // Update DONORS table
            try (PreparedStatement ps = con.prepareStatement(donorSql)) {

                ps.setInt(1, donor.getBloodGroupId());
                ps.setDate(2, donor.getDateOfBirth());
                ps.setString(3, donor.getAvailabilityStatus());

                if (donor.getLastDonationDate() != null) {
                    ps.setDate(4, donor.getLastDonationDate());
                } else {
                    ps.setNull(4, java.sql.Types.DATE);
                }

                ps.setString(5, donor.getContactConsent());
                if (donor.getLocationId() > 0) ps.setInt(6, donor.getLocationId()); else ps.setNull(6, java.sql.Types.NUMERIC);
                ps.setInt(7, userId);

                if (ps.executeUpdate() != 1) {
                    con.rollback();
                    return false;
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {

            e.printStackTrace();

            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackError) {
                    rollbackError.printStackTrace();
                }
            }

            return false;

        } finally {

            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
