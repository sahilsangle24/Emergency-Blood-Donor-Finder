
package com.ebdf.service;

import com.ebdf.dao.DonationHistoryDAO;
import com.ebdf.model.DonationHistory;

public class DonationHistoryService {

    private DonationHistoryDAO historyDAO = new DonationHistoryDAO();

    public String addDonationHistory(DonationHistory history) {

        if (history == null) {
            return "Invalid donation details";
        }

        if (history.getDonorId() <= 0) {
            return "Invalid donor ID";
        }

        if (history.getDonationDate() == null) {
            return "Donation date is required";
        }

        if (history.getUnitsDonated() < 1 || history.getUnitsDonated() > 10) {
            return "Units donated must be between 1 and 10";
        }
        if (history.getDonationDate().after(new java.sql.Date(System.currentTimeMillis()))) {
            return "Donation date cannot be in the future";
        }

        if (history.getHospitalName() == null ||
            history.getHospitalName().trim().isEmpty() || history.getHospitalName().length() > 200) {
            return "Enter a valid hospital name (maximum 200 characters)";
        }

        boolean result = historyDAO.addDonationHistory(history);

        if (result) {
            return "Donation history added successfully";
        }

        return "Failed to add donation history";
    }
}
