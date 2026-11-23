
package com.ebdf.model;

import java.sql.Timestamp;

public class DonorResponse {

    private int responseId;
    private int requestId;
    private int donorId;
    private String responseStatus;
    private Timestamp respondedAt;
    private String responseNotes;

    public DonorResponse() {
    }

    public int getResponseId() {
        return responseId;
    }

    public void setResponseId(int responseId) {
        this.responseId = responseId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getDonorId() {
        return donorId;
    }

    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Timestamp getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(Timestamp respondedAt) {
        this.respondedAt = respondedAt;
    }

    public String getResponseNotes() {
        return responseNotes;
    }

    public void setResponseNotes(String responseNotes) {
        this.responseNotes = responseNotes;
    }
}
