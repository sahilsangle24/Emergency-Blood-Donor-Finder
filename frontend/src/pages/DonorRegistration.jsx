import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function DonorRegistration() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    bloodGroupId: "",
    addressLine: "",
    city: "",
    district: "",
    state: "",
    pincode: "",
    dateOfBirth: "",
    availabilityStatus: "AVAILABLE",
    lastDonationDate: "",
    contactConsent: false,
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    setFormData({
      ...formData,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    try {
      setLoading(true);

      const payload = {
        ...formData,
        bloodGroupId: Number(formData.bloodGroupId),
        lastDonationDate: formData.lastDonationDate || null,
      };

      const response = await api.post("/donor/register", payload);

      setSuccess(
        response.data.message || "Donor profile created successfully!",
      );

      setTimeout(() => {
        navigate("/donor-dashboard");
      }, 1500);
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Donor registration failed. Please try again.",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <h1>Complete Donor Profile</h1>
        <p className="auth-subtitle">
          Provide your details to become a registered blood donor.
        </p>

        {error && <div className="auth-error">{error}</div>}
        {success && <div className="auth-success">{success}</div>}

        <form onSubmit={handleSubmit}>
          <label>Blood Group</label>
          <select
            name="bloodGroupId"
            value={formData.bloodGroupId}
            onChange={handleChange}
            required
          >
            <option value="">Select Blood Group</option>
            <option value="4">A+</option>
            <option value="5">A-</option>
            <option value="6">B+</option>
            <option value="7">B-</option>
            <option value="8">AB+</option>
            <option value="9">AB-</option>
            <option value="10">O+</option>
            <option value="11">O-</option>
          </select>

          <label>Address (Optional)</label>
          <input
            type="text"
            name="addressLine"
            placeholder="Street / area"
            value={formData.addressLine}
            onChange={handleChange}
          />

          <label>City</label>
          <input
            type="text"
            name="city"
            placeholder="Enter city"
            value={formData.city}
            onChange={handleChange}
            required
          />

          <label>District (Optional)</label>
          <input
            type="text"
            name="district"
            placeholder="Enter district"
            value={formData.district}
            onChange={handleChange}
          />

          <label>State</label>
          <input
            type="text"
            name="state"
            placeholder="Enter state"
            value={formData.state}
            onChange={handleChange}
            required
          />

          <label>Pincode</label>
          <input
            type="text"
            name="pincode"
            placeholder="6-digit pincode"
            value={formData.pincode}
            onChange={handleChange}
            pattern="[0-9]{6}"
            maxLength={6}
            required
          />

          <label>Date of Birth</label>
          <input
            type="date"
            name="dateOfBirth"
            value={formData.dateOfBirth}
            onChange={handleChange}
            required
          />

          <label>Availability</label>
          <select
            name="availabilityStatus"
            value={formData.availabilityStatus}
            onChange={handleChange}
            required
          >
            <option value="AVAILABLE">Available</option>
            <option value="UNAVAILABLE">Unavailable</option>
          </select>

          <label>Last Donation Date (Optional)</label>
          <input
            type="date"
            name="lastDonationDate"
            value={formData.lastDonationDate}
            onChange={handleChange}
          />

          <label>
            <input
              type="checkbox"
              name="contactConsent"
              checked={formData.contactConsent}
              onChange={handleChange}
              required
            />
            I agree to be contacted regarding blood donation requests.
          </label>

          <button type="submit" className="auth-submit" disabled={loading}>
            {loading ? "Saving Profile..." : "Save Donor Profile"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default DonorRegistration;
