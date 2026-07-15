/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  darkMode: "class",
  theme: {
    extend: {
      colors: {
        canvas: "#0b0d10",
        surface: "#111417",
        raised: "#161a1e",
        border: "#22262b",
        accent: {
          DEFAULT: "#5b8cff",
          dim: "#3a5bb8",
        },
        status: {
          running: "#5b8cff",
          succeeded: "#3fb668",
          failed: "#e5484d",
          retrying: "#e5a63f",
          queued: "#7a828e",
          paused: "#7a828e",
        },
      },
      fontFamily: {
        mono: ["JetBrains Mono", "ui-monospace", "SFMono-Regular", "monospace"],
        sans: ["Inter", "ui-sans-serif", "system-ui", "sans-serif"],
      },
    },
  },
  plugins: [],
};
