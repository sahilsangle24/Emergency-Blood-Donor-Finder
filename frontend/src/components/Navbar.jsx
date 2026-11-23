import { Link } from "react-router-dom";
import { Droplet } from "lucide-react";

function Navbar() {
  return (
    <header className="navbar">
      <Link to="/" className="brand">
        <span className="brand-icon">
          <Droplet size={25} fill="white" />
        </span>
        <span>
          Life<span className="brand-red">Link</span>
        </span>
      </Link>

      <nav className="nav-links">
        <Link to="/">Home</Link>
        <a href="#how-it-works">How It Works</a>
        <a href="#about">About Us</a>
        <a href="#contact">Contact</a>
      </nav>

      <div className="nav-actions">
        <Link to="/login" className="nav-login">
          Login
        </Link>
        <Link to="/register" className="nav-register">
          Become a Donor
        </Link>
      </div>
    </header>
  );
}

export default Navbar;
