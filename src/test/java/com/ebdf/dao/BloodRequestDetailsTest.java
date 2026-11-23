
package com.ebdf.dao;

import java.util.Map;

public class BloodRequestDetailsTest {

    public static void main(String[] args) {

        BloodRequestDetailsDAO dao = new BloodRequestDetailsDAO();

        try {
            // Replace 1 with an existing request ID
            Map<String, Object> request = dao.getRequestById(1);

            if (request == null) {
                System.out.println("Blood request not found.");
            } else {
                System.out.println("Blood Request Details:");

                request.forEach((key, value) ->
                    System.out.println(key + " : " + value)
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
