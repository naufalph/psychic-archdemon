/**
 * V2 Design System Typography
 * Based on Figma Design: Landing Page Freelance Arsitek
 * Font Family: Poppins
 */

module.exports = {
  fonts: {
    primary: 'Poppins, sans-serif'
  },
  typography: {
    // Display styles for hero sections
    display: {
      large: {
        fontFamily: 'Poppins',
        fontSize: '64px',
        fontWeight: 700,
        lineHeight: '1.5em',
        color: '#FFFFFF'
      },
      medium: {
        fontFamily: 'Poppins',
        fontSize: '48px',
        fontWeight: 700,
        lineHeight: '1.5em',
        color: '#000000'
      }
    },

    // Headline styles for page titles
    headline: {
      large: {
        fontFamily: 'Poppins',
        fontSize: '36px',
        fontWeight: 600,
        lineHeight: '1.5em',
        color: '#000000'
      },
      medium: {
        fontFamily: 'Poppins',
        fontSize: '24px',
        fontWeight: 600,
        lineHeight: '1.5em',
        color: '#000000'
      }
    },

    // Body text styles
    body: {
      large: {
        fontFamily: 'Poppins',
        fontSize: '16px',
        fontWeight: 400,
        lineHeight: '1.5em',
        color: '#ABABAB'
      },
      medium: {
        fontFamily: 'Poppins',
        fontSize: '14px',
        fontWeight: 400,
        lineHeight: '1.2857em',
        color: '#000000'
      },
      small: {
        fontFamily: 'Poppins',
        fontSize: '12px',
        fontWeight: 400,
        lineHeight: '1.6667em',
        color: '#64748B' // slate-500
      }
    },

    // Button styles
    button: {
      large: {
        fontFamily: 'Poppins',
        fontSize: '16px',
        fontWeight: 600,
        lineHeight: '1.5em',
        color: '#FFFFFF'
      },
      medium: {
        fontFamily: 'Poppins',
        fontSize: '14px',
        fontWeight: 600,
        lineHeight: '1.5em',
        color: '#FFFFFF'
      }
    },

    // Input label styles
    label: {
      medium: {
        fontFamily: 'Poppins',
        fontSize: '14px',
        fontWeight: 400,
        lineHeight: '1.2857em',
        color: '#000000'
      },
      small: {
        fontFamily: 'Poppins',
        fontSize: '12px',
        fontWeight: 400,
        lineHeight: '1.6667em',
        color: '#64748B'
      }
    }
  },

  // Color palette from Figma
  colors: {
    primary: '#C5A25A', // Gold
    background: {
      gradient: 'linear-gradient(180deg, rgba(158, 157, 153, 1) 0%, rgba(108, 103, 98, 1) 100%)',
      white: '#FFFFFF'
    },
    text: {
      primary: '#000000',
      secondary: '#ABABAB',
      muted: '#64748B' // slate-500
    },
    border: {
      default: '#334155', // slate-700
      light: '#E2E8F0'
    }
  }
}
