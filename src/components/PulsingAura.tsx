import React from 'react';

interface PulsingAuraProps {
  color: 'green' | 'amber' | 'red';
  active: boolean;
  className?: string;
}

export const PulsingAura: React.FC<PulsingAuraProps> = ({ color, active, className = '' }) => {
  const getGradient = () => {
    switch (color) {
      case 'green':
        return 'from-[#3DFFC4]/40 via-[#3DFFC4]/15 to-transparent';
      case 'amber':
        return 'from-[#FBBF24]/40 via-[#FBBF24]/15 to-transparent';
      case 'red':
      default:
        return 'from-[#FF5C5C]/40 via-[#FF5C5C]/15 to-transparent';
    }
  };

  return (
    <div
      className={`absolute inset-0 pointer-events-none rounded-[36px] blur-3xl transition-all duration-700 ${
        active ? 'animate-pulse-glow opacity-80' : 'opacity-40'
      } bg-gradient-radial ${getGradient()} ${className}`}
      aria-hidden="true"
    />
  );
};
