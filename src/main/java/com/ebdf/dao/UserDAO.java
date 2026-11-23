
package com.ebdf.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ebdf.model.User;
import com.ebdf.util.DBConnection;

public class UserDAO {

    // 1. Check whether email already exists
    public boolean emailExists(String email) {

        String sql = "SELECT user_id FROM USERS WHERE LOWER(email) = LOWER(?)";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, email.trim());

                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }

        } catch (SQLException e) {
            System.err.println("Email check database error:");
            e.printStackTrace();
            throw new RuntimeException("Unable to check email", e);
        }
    }

    // 2. Register a new user
    public boolean registerUser(User user) {

        String userSql = "INSERT INTO USERS "
                + "(full_name, email, phone, password_hash, role) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(userSql)) {
                ps.setString(1, user.getFullName().trim());
                ps.setString(2, user.getEmail().trim());
                ps.setString(3, user.getPhone().trim());
                ps.setString(4, user.getPasswordHash());
                ps.setString(5, user.getRole());

                if (ps.executeUpdate() != 1) {
                    con.rollback();
                    return false;
                }
            }

            if ("REQUESTER".equals(user.getRole())) {
                String requesterSql = """
                    INSERT INTO REQUESTERS (USER_ID)
                    SELECT USER_ID FROM USERS WHERE LOWER(EMAIL) = LOWER(?)
                    """;
                try (PreparedStatement ps = con.prepareStatement(requesterSql)) {
                    ps.setString(1, user.getEmail().trim());
                    if (ps.executeUpdate() != 1) {
                        con.rollback();
                        return false;
                    }
                }
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Registration database error:");
            e.printStackTrace();
            throw new RuntimeException("Unable to register user", e);
        }
    }

    // 3. Find user by email for login
    public User findByEmail(String email) {

        String sql = "SELECT user_id, full_name, email, phone, "
                + "password_hash, role, account_status "
                + "FROM USERS WHERE LOWER(email) = LOWER(?)";

        try (Connection con = DBConnection.getConnection()) {

            if (con == null) {
                throw new SQLException("Database connection failed");
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, email.trim());

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        User user = new User();

                        user.setUserId(rs.getInt("user_id"));
                        user.setFullName(rs.getString("full_name"));
                        user.setEmail(rs.getString("email"));
                        user.setPhone(rs.getString("phone"));
                        user.setPasswordHash(
                                rs.getString("password_hash"));
                        user.setRole(rs.getString("role"));
                        user.setAccountStatus(
                                rs.getString("account_status"));

                        return user;
                    }
                }
            }

        } catch (SQLException e) {

            System.err.println("Find user database error:");
            e.printStackTrace();

            throw new RuntimeException("Unable to find user", e);
        }

        return null;
    }
}
