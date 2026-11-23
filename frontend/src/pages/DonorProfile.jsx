import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  User,
  Mail,
  Phone,
  Droplet,
  MapPin,
  Calendar,
  Edit,
  Save,
  X,
} from "lucide-react";
import api from "../services/api";

const bloodGroups = [
  { id: 4, name: "A+" },
  { id: 5, name: "A-" },
  { id: 6, name: "B+" },
  { id: 7, name: "B-" },
  { id: 8, name: "AB+" },
  { id: 9, name: "AB-" },
  { id: 10, name: "O+" },
  { id: 11, name: "O-" },
];

function DonorProfile() {
  const [profile, setProfile] = useState(null);
  const [formData, setFormData] = useState({});
  const [editing, setEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const fetchProfile = async () => {
    try {
      const response = await api.get("/donor/profile");
      setProfile(response.data);
      setFormData({
        phone: response.data.phone || "",
        bloodGroupId: response.data.bloodGroupId || "",
        dateOfBirth: response.data.dateOfBirth || "",
        availabilityStatus: response.data.availabilityStatus || "AVAILABLE",
        lastDonationDate: response.data.lastDonationDate || "",
        contactConsent: response.data.contactConsent === "Y",
        city: response.data.city || "", state: response.data.state || "", pincode: response.data.pincode || "", district: response.data.district || "",
      });
    } catch (err) {
      setError(err.response?.data?.message || "Unable to load donor profile.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void Promise.resolve().then(fetchProfile);
  }, []);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    setFormData({
      ...formData,
      [name]: type === "checkbox" ? checked : value,
    });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");

    try {
      setSaving(true);

      const payload = {
        ...formData,
        bloodGroupId: Number(formData.bloodGroupId),
        lastDonationDate: formData.lastDonationDate || null,
      };

      await api.put("/donor/profile", payload);

      await fetchProfile();
      setEditing(false);
      setSuccess("Profile updated successfully!");
    } catch (err) {
      setError(
        err.response?.data?.message || "Unable to update donor profile.",
      );
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <h2>Loading donor profile...</h2>;
  if (!profile) return <p>{error || "Donor profile not found."}</p>;

  return (
    <div className="dashboard-page">
      <div className="dashboard-content">
        <h1>My Donor Profile</h1>
        <p>Personal and donation information</p>

        {error && <div className="auth-error">{error}</div>}
        {success && <div className="auth-success">{success}</div>}

        <div className="profile-card">
          <div className="profile-header">
            <div className="profile-avatar">
              {profile.fullName?.charAt(0).toUpperCase()}
            </div>

            <div>
              <h2>{profile.fullName}</h2>
              <p>{profile.bloodGroup} Blood Donor</p>
            </div>
          </div>

          {editing ? (
            <form onSubmit={handleSave} className="profile-details">
              <label>Phone Number</label>
              <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                pattern="[0-9]{10,15}"
                required
              />

              <label>Blood Group</label>
              <select
                name="bloodGroupId"
                value={formData.bloodGroupId}
                onChange={handleChange}
                required
              >
                <option value="">Select Blood Group</option>
                {bloodGroups.map((group) => (
                  <option key={group.id} value={group.id}>
                    {group.name}
                  </option>
                ))}
              </select>

              <label>City</label><input name="city" value={formData.city || ""} onChange={handleChange} required />
              <label>State</label><input name="state" value={formData.state || ""} onChange={handleChange} required />
              <label>PIN Code</label><input name="pincode" value={formData.pincode || ""} onChange={handleChange} pattern="[0-9]{6}" maxLength={6} required />
              <label>District</label><input name="district" value={formData.district || ""} onChange={handleChange} />
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
              >
                <option value="AVAILABLE">Available</option>
                <option value="UNAVAILABLE">Unavailable</option>
              </select>

              <label>Last Donation Date</label>
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
                />
                Allow contact for donation requests
              </label>

              <button type="submit" className="auth-submit" disabled={saving}>
                <Save size={17} />
                {saving ? "Saving..." : "Save Changes"}
              </button>

              <button
                type="button"
                className="auth-submit"
                onClick={() => {
                  setEditing(false);
                  setError("");
                  fetchProfile();
                }}
              >
                <X size={17} /> Cancel
              </button>
            </form>
          ) : (
            <>
              <div className="profile-details">
                <ProfileItem
                  icon={User}
                  label="Full Name"
                  value={profile.fullName}
                />
                <ProfileItem icon={Mail} label="Email" value={profile.email} />
                <ProfileItem icon={Phone} label="Phone" value={profile.phone} />
                <ProfileItem
                  icon={Droplet}
                  label="Blood Group"
                  value={profile.bloodGroup}
                />
                <ProfileItem
                  icon={Calendar}
                  label="Date of Birth"
                  value={profile.dateOfBirth}
                />
                <ProfileItem
                  icon={User}
                  label="Availability"
                  value={profile.availabilityStatus}
                />
                <ProfileItem
                  icon={MapPin}
                  label="City"
                  value={profile.city || "Not provided"}
                />
                <ProfileItem
                  icon={Calendar}
                  label="Last Donation"
                  value={profile.lastDonationDate || "Not provided"}
                />
              </div>

              <button
                className="auth-submit"
                onClick={() => {
                  setEditing(true);
                  setSuccess("");
                }}
              >
                <Edit size={17} /> Edit Profile
              </button>
            </>
          )}

          <Link to="/donor-dashboard" className="auth-switch">
            Back to Dashboard
          </Link>
        </div>
      </div>
    </div>
  );
}

function ProfileItem({ icon: Icon, label, value }) {
  return (
    <div className="profile-detail">
      <Icon size={20} />
      <div>
        <strong>{label}</strong>
        <p>{value || "Not provided"}</p>
      </div>
    </div>
  );
}

export default DonorProfile;
