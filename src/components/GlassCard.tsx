import React from 'react';

interface GlassCardProps {
  children: React.ReactNode;
  className?: string;
  borderAccent?: 'green' | 'teal' | 'red' | 'amber' | 'default';
  onClick?: () => void;
  hoverable?: boolean;
}

export const GlassCard: React.FC<GlassCardProps> = ({
  children,
  className = '',
  borderAccent = 'default',
  onClick,
  hoverable = false,
}) => {
  const getBorderGradient = () => {
    switch (borderAccent) {
      case 'green':
        return 'border-[#3DFFC4]/30 hover:border-[#3DFFC4]/50 shadow-[0_8px_32px_rgba(61,255,196,0.06)]';
      case 'teal':
        return 'border-[#2DD4BF]/30 hover:border-[#2DD4BF]/50 shadow-[0_8px_32px_rgba(45,212,191,0.06)]';
      case 'red':
        return 'border-[#FF5C5C]/30 hover:border-[#FF5C5C]/50 shadow-[0_8px_32px_rgba(255,92,92,0.06)]';
      case 'amber':
        return 'border-[#FBBF24]/30 hover:border-[#FBBF24]/50 shadow-[0_8px_32px_rgba(251,191,36,0.06)]';
      case 'default':
      default:
        return 'border-white/[0.08] hover:border-white/[0.16] shadow-[0_8px_32px_rgba(0,0,0,0.35)]';
    }
  };

  return (
    <div
      onClick={onClick}
      className={`relative rounded-[24px] glass-panel border ${getBorderGradient()} transition-all duration-300 ease-spring ${
        hoverable ? 'hover:-translate-y-0.5 hover:shadow-xl cursor-pointer' : ''
      } ${className}`}
    >
      {children}
    </div>
  );
};
