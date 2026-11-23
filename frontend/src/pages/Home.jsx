import { Link } from "react-router-dom";
import {
  Search,
  Heart,
  Users,
  Droplet,
  ArrowRight,
  ShieldCheck,
  Clock,
  MapPin,
} from "lucide-react";

import Navbar from "../components/Navbar";
import Footer from "../components/Footer";

function Home() {
  return (
    <>
      <Navbar />

      <main>
        {/* Hero Section */}
        <section className="hero">
          <div className="hero-content">
            <div className="hero-label">
              <Heart size={16} fill="currentColor" />
              Every Drop Counts
            </div>

            <h1>
              A Little Help Can
              <span> Save A Life.</span>
            </h1>

            <p>
              Find blood donors near you, connect with generous people, and make
              a difference when every second matters.
            </p>

            <div className="hero-actions">
              <Link to="/find-donors" className="primary-btn">
                <Search size={19} />
                Find Blood Donors
                <ArrowRight size={18} />
              </Link>

              <Link to="/register" className="outline-btn">
                Become a Donor
              </Link>
            </div>

            <div className="hero-trust">
              <span>
                <ShieldCheck size={17} /> Secure Platform
              </span>
              <span>
                <Clock size={17} /> Emergency Focused
              </span>
            </div>
          </div>

          <div className="hero-visual">
            <div className="blood-circle">
              <Droplet size={100} fill="currentColor" />
            </div>

            <div className="floating-card card-one">
              <Heart size={20} fill="#DC2626" color="#DC2626" />
              <div>
                <strong>Be a Hero</strong>
                <small>Donate blood, save lives</small>
              </div>
            </div>

            <div className="floating-card card-two">
              <MapPin size={20} color="#DC2626" />
              <div>
                <strong>Find Nearby Donors</strong>
                <small>Connect by location</small>
              </div>
            </div>
          </div>
        </section>

        {/* Information Strip */}
        <section className="info-strip">
          <div>
            <Droplet size={25} />
            <span>Find compatible donors</span>
          </div>
          <div>
            <Users size={25} />
            <span>Connect with volunteers</span>
          </div>
          <div>
            <Heart size={25} />
            <span>Support emergency requests</span>
          </div>
        </section>

        {/* How It Works */}
        <section className="how-section" id="how-it-works">
          <div className="section-heading">
            <span>HOW IT WORKS</span>
            <h2>Help is Just Three Steps Away</h2>
            <p>A simple process to connect people and save lives.</p>
          </div>

          <div className="steps-grid">
            <div className="step-card">
              <div className="step-icon">
                <Search size={30} />
              </div>
              <h3>Find Donors</h3>
              <p>Search available donors using blood group and city.</p>
              <span>01</span>
            </div>

            <div className="step-card">
              <div className="step-icon">
                <Users size={30} />
              </div>
              <h3>Connect</h3>
              <p>
                Submit an emergency request and connect with willing donors.
              </p>
              <span>02</span>
            </div>

            <div className="step-card">
              <div className="step-icon">
                <Heart size={30} />
              </div>
              <h3>Save Lives</h3>
              <p>Coordinate with the hospital and help patients in need.</p>
              <span>03</span>
            </div>
          </div>
        </section>

        {/* Call to Action */}
        <section className="cta-section" id="about">
          <div>
            <span>BE SOMEONE'S HERO</span>
            <h2>Your One Donation Can Make a Difference.</h2>
            <p>Register as a donor and make yourself available to help.</p>
          </div>

          <Link to="/register" className="cta-button">
            Register Now <ArrowRight size={18} />
          </Link>
        </section>
      </main>

      <Footer />
    </>
  );
}

export default Home;
