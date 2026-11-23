
package com.ebdf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.ebdf.model.Location;
import com.ebdf.util.DBConnection;

public class LocationDAO {

    // Insert location and return generated location ID
    public int addLocation(Location location) {

        String sql = "INSERT INTO LOCATIONS "
                + "(address_line, city, district, state, pincode, latitude, longitude) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, location.getAddressLine());
            ps.setString(2, location.getCity());
            ps.setString(3, location.getDistrict());
            ps.setString(4, location.getState());
            ps.setString(5, location.getPincode());
            ps.setDouble(6, location.getLatitude());
            ps.setDouble(7, location.getLongitude());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
