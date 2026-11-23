
package com.ebdf.service;

import com.ebdf.dao.DonorResponseDAO;

public class DonorResponseService {

    private DonorResponseDAO responseDAO = new DonorResponseDAO();

    public String respondToRequest(
            int requestId,
            int donorId,
            String responseStatus,
            String responseNotes) {

        if (requestId <= 0) {
            return "Invalid request ID";
        }

        if (donorId <= 0) {
            return "Invalid donor ID";
        }

        if (responseStatus == null ||
            !(responseStatus.equals("ACCEPTED") ||
              responseStatus.equals("REJECTED"))) {
            return "Response must be ACCEPTED or REJECTED";
        }

        boolean result = responseDAO.respondToRequest(
                requestId,
                donorId,
                responseStatus,
                responseNotes
        );

        if (result) {
            return "Response submitted successfully";
        }

        return "Failed to submit response";
    }
}
