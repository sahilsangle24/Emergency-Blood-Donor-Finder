import { Droplet } from "lucide-react";

function Footer() {
  return (
    <footer className="footer" id="contact">
      <div className="footer-brand">
        <Droplet size={24} />
        <h2>LifeLink</h2>
      </div>

      <p>Connecting blood donors with people who need them.</p>

      <p className="copyright">
        © 2026 LifeLink. Emergency Blood Donor Finder.
      </p>
    </footer>
  );
}

export default Footer;
