import axios from "axios";

// In development, Vite proxies /api to Tomcat. In the packaged WAR,
// the same-origin API lives at the application's /api context.
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  headers: { "Content-Type": "application/json" },
  withCredentials: true,
  timeout: 20000,
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.code === "ECONNABORTED") {
      error.userMessage = "The server took too long to respond. Please try again.";
    } else if (!error.response) {
      error.userMessage = "Cannot reach the application server. Check that Tomcat is running.";
    } else if (error.response.status === 401) {
      const url = String(error.config?.url || "");
      if (!["/login", "/register", "/health", "/logout"].includes(url)) {
        sessionStorage.clear();
        window.location.assign(`${import.meta.env.BASE_URL}login`);
      }
    }
    return Promise.reject(error);
  },
);

export default api;
