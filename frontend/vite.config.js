/* global process */
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

const appContext = process.env.EBDF_TOMCAT_CONTEXT || "/EmergencyBloodDonorFinder";
const assetBase = process.env.VITE_ASSET_BASE || `${appContext}/app/`;

export default defineConfig(({ mode }) => ({
  // Vite development is served at /; production assets live inside the WAR at /app/.
  base: mode === "development" ? "/" : assetBase,
  plugins: [react()],
  server: {
    host: "127.0.0.1",
    port: 5173,
    strictPort: true,
    proxy: {
      "/api": {
        target: process.env.EBDF_TOMCAT_ORIGIN || "http://localhost:8086",
        changeOrigin: true,
        // Tomcat sets JSESSIONID Path to its context; rewrite it for Vite's /api path.
        cookiePathRewrite: "/",
        cookieDomainRewrite: "",
        rewrite: (path) => `${appContext}${path}`,
      },
    },
  },
  build: {
    outDir: "../src/main/webapp/app",
    emptyOutDir: true,
    sourcemap: false,
    manifest: true,
  },
}));
