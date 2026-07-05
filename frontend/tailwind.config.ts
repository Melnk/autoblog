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
        surface: {
          950: "#050914",
          900: "#080e1c",
          850: "#0c1426",
          800: "#111b31",
          700: "#1b2944"
        },
        neon: {
          blue: "#2f8cff",
          cyan: "#21d4fd",
          violet: "#935cff",
          green: "#21e6a7"
        }
      },
      boxShadow: {
        glow: "0 0 36px rgba(47, 140, 255, 0.22)",
        "glow-violet": "0 0 36px rgba(147, 92, 255, 0.2)"
      }
    }
  },
  plugins: []
};

export default config;
