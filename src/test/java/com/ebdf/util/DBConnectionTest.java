package com.ebdf.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBConnectionTest {

    public static void main(String[] args) {

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            ResultSet rs = st.executeQuery(
                "SELECT SYS_CONTEXT('USERENV', 'CON_NAME') FROM DUAL"
            );

            if (rs.next()) {
                System.out.println("Connected Successfully!");
                System.out.println("Database User: "
                        + con.getMetaData().getUserName());
                System.out.println("Container: " + rs.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}