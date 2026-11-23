
package com.ebdf.service;

import java.util.Map;

import com.ebdf.dao.DonorDAO;
import com.ebdf.model.Donor;

public class DonorService {

    private DonorDAO donorDAO = new DonorDAO();

    // 1. Register donor profile
    public String registerDonor(Donor donor) {

        if (donor == null) {
            return "Invalid donor details";
        }

        if (donor.getUserId() <= 0) {
            return "Invalid user ID";
        }

        if (donor.getBloodGroupId() <= 0) {
            return "Please select a blood group";
        }

        if (donor.getDateOfBirth() == null) {
            return "Date of birth is required";
        }

        if (donor.getDateOfBirth().after(new java.sql.Date(System.currentTimeMillis()))) {
            return "Date of birth cannot be in the future";
        }

        if (donor.getAvailabilityStatus() == null ||
            !("AVAILABLE".equals(donor.getAvailabilityStatus()) ||
              "UNAVAILABLE".equals(donor.getAvailabilityStatus()))) {
            return "Select a valid availability status";
        }

        if (donor.getContactConsent() == null ||
            !("Y".equals(donor.getContactConsent()) || "N".equals(donor.getContactConsent()))) {
            return "Select whether you consent to be contacted";
        }

        boolean result = donorDAO.registerDonor(donor);

        if (result) {
            return "Donor registration successful";
        }

        return "Donor registration failed";
    }

    // 2. Get donor profile
    public Map<String, Object> getDonorProfile(int userId) {

        if (userId <= 0) {
            return null;
        }

        return donorDAO.getDonorProfileByUserId(userId);
    }

    // 3. Update donor profile
    public String updateDonorProfile(
            int userId, Donor donor, String phone) {

        if (userId <= 0 || donor == null) {
            return "Invalid donor details";
        }

        if (phone == null ||
            !phone.matches("[0-9]{10,15}")) {
            return "Enter a valid phone number";
        }

        if (donor.getBloodGroupId() <= 0) {
            return "Please select a valid blood group";
        }

        if (donor.getDateOfBirth() == null) {
            return "Date of birth is required";
        }
        if (donor.getDateOfBirth().after(new java.sql.Date(System.currentTimeMillis()))) {
            return "Date of birth cannot be in the future";
        }
        if (donor.getAvailabilityStatus() == null ||
            !("AVAILABLE".equals(donor.getAvailabilityStatus()) ||
              "UNAVAILABLE".equals(donor.getAvailabilityStatus()))) {
            return "Select a valid availability status";
        }
        if (donor.getContactConsent() == null ||
            !("Y".equals(donor.getContactConsent()) || "N".equals(donor.getContactConsent()))) {
            return "Select whether you consent to be contacted";
        }

        boolean result =
                donorDAO.updateDonorProfile(userId, donor, phone);

        if (result) {
            return "Donor profile updated successfully";
        }

        return "Donor profile update failed";
    }
}
