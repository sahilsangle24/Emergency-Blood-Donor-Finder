import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Mail, Lock, Eye, EyeOff, Droplet } from "lucide-react";
import api from "../services/api";

function Login() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!formData.email || !formData.password) {
      setError("Please enter your email and password.");
      return;
    }

    try {
      setLoading(true);

      const response = await api.post("/login", formData);
      const user = response.data;

      sessionStorage.setItem("userId", user.userId);
      sessionStorage.setItem("userRole", user.role);
      sessionStorage.setItem("fullName", user.fullName);

      if (user.role === "DONOR") {
        try {
          await api.get("/donor/profile");
          navigate("/donor-dashboard");
        } catch (err) {
          if (err.response?.status === 404) {
            navigate("/donor-registration");
          } else {
            setError(
              err.response?.data?.message || "Unable to verify donor profile.",
            );
          }
        }
      } else if (user.role === "REQUESTER") {
        navigate("/requester-dashboard");
      } else if (user.role === "ADMIN") {
        navigate("/admin-dashboard");
      } else {
        setError("Unknown user role.");
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
          "Login failed. Please check your credentials.",
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <Link to="/" className="auth-brand">
          <Droplet size={30} fill="currentColor" />
          LifeLink
        </Link>

        <h1>Welcome Back!</h1>

        <p className="auth-subtitle">
          Login to your Emergency Blood Donor Finder account.
        </p>

        {error && <div className="auth-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <label>Email Address</label>

          <div className="auth-input">
            <Mail size={19} />
            <input
              type="email"
              name="email"
              placeholder="Enter your email"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <label>Password</label>

          <div className="auth-input">
            <Lock size={19} />
            <input
              type={showPassword ? "text" : "password"}
              name="password"
              placeholder="Enter your password"
              value={formData.password}
              onChange={handleChange}
              required
            />

            <button
              type="button"
              className="password-toggle"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? <EyeOff size={19} /> : <Eye size={19} />}
            </button>
          </div>

          <button type="submit" className="auth-submit" disabled={loading}>
            {loading ? "Logging in..." : "Login to Account"}
          </button>
        </form>

        <p className="auth-switch">
          Don't have an account? <Link to="/register">Register Now</Link>
        </p>
      </div>
    </div>
  );
}

export default Login;
