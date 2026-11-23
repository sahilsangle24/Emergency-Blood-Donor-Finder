import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../..");
const frontend = path.join(root, "frontend/src");
const java = path.join(root, "src/main/java");
const files = (dir) => fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) =>
  entry.isDirectory() ? files(path.join(dir, entry.name)) : [path.join(dir, entry.name)],
);
const mappings = new Set();
for (const file of files(java).filter((name) => name.endsWith(".java"))) {
  const source = fs.readFileSync(file, "utf8");
  for (const match of source.matchAll(/@WebServlet\(\s*"([^"]+)"\s*\)/g)) mappings.add(match[1]);
}
const used = new Set();
for (const file of files(frontend).filter((name) => /\.(jsx?|tsx?)$/.test(name))) {
  const source = fs.readFileSync(file, "utf8");
  for (const match of source.matchAll(/api\.(?:get|post|put|patch|delete)\(\s*["']([^"']+)["']/g)) {
    const rawRoute = match[1].split("?")[0];
    if (rawRoute.startsWith("/")) used.add(rawRoute.startsWith("/api/") ? rawRoute : `/api${rawRoute}`);
  }
}
const normalize = (route) => route.replace(/\/\*$/, "");
const missing = [...used].filter((route) => ![...mappings].some((mapping) => {
  const base = normalize(mapping);
  return route === base || route.startsWith(`${base}/`) || (mapping.endsWith("/*") && route.startsWith(base));
}));
if (missing.length) {
  console.error("Frontend API routes without a servlet mapping:", missing.join(", "));
  process.exit(1);
}
console.log(`API contract check passed: ${used.size} frontend API routes checked against ${mappings.size} servlet mappings.`);
