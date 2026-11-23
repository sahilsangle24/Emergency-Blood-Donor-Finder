
package com.ebdf.model;

import java.sql.Timestamp;

public class BloodRequest {

    private int requestId;
    private int requesterUserId;
    private int bloodGroupId;
    private int locationId;
    private String hospitalName;
    private String hospitalAddress;
    private int unitsRequired;
    private String urgency;
    private Timestamp requiredBy;
    private String requestDetails;
    private String requestStatus;

    public BloodRequest() {
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(int requesterUserId) {
        this.requesterUserId = requesterUserId;
    }

    public int getBloodGroupId() {
        return bloodGroupId;
    }

    public void setBloodGroupId(int bloodGroupId) {
        this.bloodGroupId = bloodGroupId;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getHospitalAddress() {
        return hospitalAddress;
    }

    public void setHospitalAddress(String hospitalAddress) {
        this.hospitalAddress = hospitalAddress;
    }

    public int getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(int unitsRequired) {
        this.unitsRequired = unitsRequired;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public Timestamp getRequiredBy() {
        return requiredBy;
    }

    public void setRequiredBy(Timestamp requiredBy) {
        this.requiredBy = requiredBy;
    }

    public String getRequestDetails() {
        return requestDetails;
    }

    public void setRequestDetails(String requestDetails) {
        this.requestDetails = requestDetails;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }
}
