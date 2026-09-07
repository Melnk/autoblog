import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./src/app/**/*.{ts,tsx}",
    "./src/components/**/*.{ts,tsx}",
    "./src/lib/**/*.{ts,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        canvas: "#F6F6F2",
        ink: "#242424",
        muted: "#6B6B63",
        line: "#E8E8E2",
        brand: { yellow: "#FFD337", soft: "#FFF5C2", ink: "#242424" }
      },
      boxShadow: {
        card: "0 4px 24px rgba(36, 36, 36, 0.035)",
        lift: "0 12px 32px rgba(36, 36, 36, 0.08)"
      }
    }
  },
  plugins: []
};

export default config;
