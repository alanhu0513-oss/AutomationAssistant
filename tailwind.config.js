/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: '#080B10',
        midnight: '#080B10',
        surface: {
          DEFAULT: '#0E141B',
          variant: '#151C25',
          lowest: '#0A0F15',
          low: '#11171F',
          mid: '#141B24',
          high: '#19212B',
          highest: '#1F2833',
        },
        neon: {
          green: '#3DFFC4',
          teal: '#2DD4BF',
          red: '#FF5C5C',
          amber: '#FBBF24',
        },
        borderDark: '#2A3542',
      },
      fontFamily: {
        sans: [
          'Inter',
          '-apple-system',
          'BlinkMacSystemFont',
          'Segoe UI',
          'Roboto',
          'Helvetica Neue',
          'Arial',
          'sans-serif',
        ],
      },
      animation: {
        'pulse-glow': 'pulseGlow 2.2s infinite alternate ease-in-out',
        'fade-in': 'fadeIn 0.25s ease-out',
      },
      keyframes: {
        pulseGlow: {
          '0%': { opacity: '0.35', transform: 'scale(0.98)' },
          '100%': { opacity: '0.75', transform: 'scale(1.02)' },
        },
        fadeIn: {
          '0%': { opacity: '0', transform: 'translateY(4px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
      },
    },
  },
  plugins: [],
}
