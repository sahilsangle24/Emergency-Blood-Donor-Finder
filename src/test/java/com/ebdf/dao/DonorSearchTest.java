
package com.ebdf.dao;

import java.util.List;

public class DonorSearchTest {

    public static void main(String[] args) {

        DonorSearchDAO dao = new DonorSearchDAO();

        try {
            List<java.util.Map<String,Object>> donors = dao.searchAvailableDonors(4,"Nashik");

            if (donors.isEmpty()) {
                System.out.println("No eligible donors found.");
            } else {
                System.out.println("Available Donors:");

                for (java.util.Map<String,Object> donor : donors) {
                    System.out.println(donor);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
