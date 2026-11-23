
package com.ebdf.service;

import com.ebdf.dao.BloodRequestDAO;
import com.ebdf.model.BloodRequest;

public class BloodRequestService {

    private BloodRequestDAO bloodRequestDAO = new BloodRequestDAO();

    public String createRequest(BloodRequest request) {
        return createRequestWithId(request) > 0 ? "Blood request created successfully" : "Blood request creation failed";
    }

    public int createRequestWithId(BloodRequest request) {

        if (request == null) {
            return -1;
        }

        if (request.getRequesterUserId() <= 0) {
            return -1;
        }

        if (request.getBloodGroupId() <= 0) {
            return -1;
        }

        if (request.getUnitsRequired() < 1 || request.getUnitsRequired() > 10) {
            return -1;
        }

        if (request.getHospitalName() == null ||
            request.getHospitalName().trim().isEmpty() || request.getHospitalName().length() > 200) {
            return -1;
        }
        if (request.getHospitalAddress() == null ||
            request.getHospitalAddress().trim().isEmpty() || request.getHospitalAddress().length() > 500) {
            return -1;
        }

        if (request.getUrgency() == null ||
            !(request.getUrgency().equals("NORMAL") ||
              request.getUrgency().equals("URGENT") ||
              request.getUrgency().equals("CRITICAL"))) {
            return -1;
        }

        if (request.getRequiredBy() == null ||
            request.getRequiredBy().before(new java.sql.Timestamp(System.currentTimeMillis()))) {
            return -1;
        }

        return bloodRequestDAO.createRequestWithId(request);
    }
}
