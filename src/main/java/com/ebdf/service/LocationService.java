
package com.ebdf.service;

import com.ebdf.dao.LocationDAO;
import com.ebdf.model.Location;

public class LocationService {

    private LocationDAO locationDAO = new LocationDAO();

    public String addLocation(Location location) {

        if (location == null) {
            return "Invalid location details";
        }

        if (location.getCity() == null ||
            location.getCity().trim().isEmpty()) {
            return "City is required";
        }

        if (location.getState() == null ||
            location.getState().trim().isEmpty()) {
            return "State is required";
        }

        if (location.getPincode() == null ||
            !location.getPincode().matches("\\d{6}")) {
            return "Enter a valid 6-digit pincode";
        }

        int locationId = locationDAO.addLocation(location);

        if (locationId > 0) {
            return "Location added successfully. ID: " + locationId;
        }

        return "Failed to add location";
    }
}
