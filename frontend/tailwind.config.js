/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{vue,js,ts,jsx,tsx}',
  ],
  theme: {
    extend: {
      colors: {
        apple: {
          bg: '#1c1c1e',
          'bg-secondary': '#2c2c2e',
          'bg-tertiary': '#3a3a3c',
          'bg-elevated': '#1c1c1e',
          card: '#2c2c2e',
          separator: '#38383a',
          'separator-light': '#48484a',
          accent: '#0a84ff',
          'accent-hover': '#409cff',
          green: '#30d158',
          red: '#ff453a',
          orange: '#ff9f0a',
          yellow: '#ffd60a',
        },
      },
      borderRadius: {
        apple: '10px',
        'apple-lg': '14px',
      },
      boxShadow: {
        apple: '0 2px 10px rgba(0, 0, 0, 0.3)',
        'apple-lg': '0 8px 32px rgba(0, 0, 0, 0.4)',
      },
      backdropBlur: {
        apple: '20px',
      },
      fontSize: {
        '2xs': ['0.625rem', { lineHeight: '0.875rem' }],
      },
    },
  },
  plugins: [],
}
