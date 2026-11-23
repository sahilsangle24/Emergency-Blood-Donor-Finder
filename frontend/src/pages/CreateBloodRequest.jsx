import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../services/api";
import {
  Droplet,
  ArrowLeft,
  MapPin,
  Building2,
  CalendarClock,
  AlertCircle,
  FileText,
  Send,
  Heart,
} from "lucide-react";

import "./CreateBloodRequest.css";

function CreateBloodRequest() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    bloodGroup: "",
    hospitalName: "",
    hospitalAddress: "",
    city: "",
    unitsRequired: "1",
    urgency: "NORMAL",
    requiredBy: "",
    requestDetails: "",
  });

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");

    const bloodGroupIds = {
      "A+": 4, "A-": 5, "B+": 6, "B-": 7,
      "AB+": 8, "AB-": 9, "O+": 10, "O-": 11,
    };

    if (!bloodGroupIds[formData.bloodGroup]) {
      setError("Please select a valid blood group.");
      return;
    }

    if (new Date(formData.requiredBy).getTime() <= Date.now()) {
      setError("Required date and time must be in the future.");
      return;
    }

    try {
      setLoading(true);
      const payload = {
        bloodGroupId: bloodGroupIds[formData.bloodGroup],
        unitsRequired: Number(formData.unitsRequired),
        hospitalName: formData.hospitalName.trim(),
        hospitalAddress: `${formData.hospitalAddress.trim()}, ${formData.city.trim()}`,
        urgency: formData.urgency,
        requiredBy: formData.requiredBy,
        requestDetails: [
          formData.requestDetails.trim(),
        ].filter(Boolean).join("\\n"),
      };

      const response = await api.post("/blood-request", payload);
      setMessage(response.data.message || "Blood request submitted successfully.");
      window.setTimeout(() => navigate("/my-blood-requests"), 900);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        "Unable to submit the request. Please sign in as a requester and try again."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-request-page">
      <header className="create-request-header">
        <Link to="/" className="create-request-brand">
          <Droplet size={27} fill="currentColor" />
          LifeLink
        </Link>

        <Link to="/requester-dashboard" className="requester-back-link">
          <ArrowLeft size={17} />
          Back to Dashboard
        </Link>
      </header>

      <main className="create-request-container">
        <section className="create-request-heading">
          <div className="create-heading-icon">
            <Heart size={29} />
          </div>

          <span>EMERGENCY BLOOD SUPPORT</span>

          <h1>Create Blood Request</h1>

          <p>
            Provide the hospital and blood requirement details needed to coordinate with potential donors.
          </p>
        </section>

        <form className="blood-request-form" onSubmit={handleSubmit}>
          <div className="request-form-title">
            <div>
              <h2>Request Information</h2>
              <p>Fill in all required details.</p>
            </div>

            <span>* Required fields</span>
          </div>

          <div className="request-privacy-note" role="note">
            Please do not enter patient names, phone numbers, medical record numbers, or other identifying medical details. Share only the information needed to coordinate with the hospital.
          </div>

          {/* Hospital Information */}
          <div className="form-section-title">
            <Building2 size={20} />
            <h3>Hospital Information</h3>
          </div>

          <div className="request-form-grid">
            <div className="request-field">
              <label>Hospital Name *</label>
              <input
                type="text"
                name="hospitalName"
                placeholder="Enter hospital name"
                value={formData.hospitalName}
                onChange={handleChange}
                required
              />
            </div>

            <div className="request-field">
              <label>City *</label>
              <input
                type="text"
                name="city"
                placeholder="Enter city"
                value={formData.city}
                onChange={handleChange}
                required
              />
            </div>

            <div className="request-field full-width">
              <label>Hospital Address *</label>
              <div className="request-input-icon">
                <MapPin size={18} />
                <input
                  type="text"
                  name="hospitalAddress"
                  placeholder="Enter complete hospital address"
                  value={formData.hospitalAddress}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>
          </div>

          {/* Emergency Details */}
          <div className="form-section-title">
            <AlertCircle size={20} />
            <h3>Emergency Details</h3>
          </div>

          <div className="request-form-grid">
            <div className="request-field">
              <label>Urgency Level *</label>
              <select
                name="urgency"
                value={formData.urgency}
                onChange={handleChange}
                required
              >
                <option value="NORMAL">Normal</option>
                <option value="URGENT">Urgent</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>

            <div className="request-field">
              <label>Required By *</label>
              <div className="request-input-icon">
                <CalendarClock size={18} />
                <input
                  type="datetime-local"
                  name="requiredBy"
                  value={formData.requiredBy}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="request-field full-width">
              <label>Additional Details</label>
              <div className="request-input-icon textarea-icon">
                <FileText size={18} />
                <textarea
                  name="requestDetails"
                  placeholder="Provide any additional information about the blood requirement..."
                  value={formData.requestDetails}
                  onChange={handleChange}
                  rows="4"
                />
              </div>
            </div>
          </div>

          {error && <div className="auth-error" role="alert">{error}</div>}
          {message && <div className="auth-success" role="status">{message}</div>}

          <div className="request-form-footer">
            <p>
              <AlertCircle size={17} />
              Please verify all information before submitting.
            </p>

            <button type="submit" className="submit-blood-request" disabled={loading}>
              <Send size={18} />
              {loading ? "Submitting..." : "Submit Blood Request"}
            </button>
          </div>
        </form>

        <div className="request-medical-note">
          Blood group and transfusion compatibility must be independently
          verified by qualified medical professionals.
        </div>
      </main>
    </div>
  );
}

export default CreateBloodRequest;
