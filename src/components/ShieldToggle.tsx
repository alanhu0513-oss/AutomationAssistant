import React from 'react';
import { Power, Shield, ShieldOff } from 'lucide-react';
import { GlassCard } from './GlassCard';
import { translations, Locale } from '../i18n/translations';

interface ShieldToggleProps {
  running: boolean;
  locale: Locale;
  onToggle: () => void;
}

export const ShieldToggle: React.FC<ShieldToggleProps> = ({ running, locale, onToggle }) => {
  const t = translations[locale];

  return (
    <GlassCard
      borderAccent={running ? 'green' : 'red'}
      onClick={onToggle}
      className="p-5 cursor-pointer hover:border-white/20 transition-all duration-300 group"
    >
      <div className="flex items-center justify-between gap-4">
        {/* Left Description */}
        <div className="flex items-center gap-3.5 min-w-0">
          <div
            className={`w-11 h-11 rounded-2xl flex items-center justify-center transition-all duration-300 ${
              running
                ? 'bg-[#3DFFC4]/15 text-[#3DFFC4] border border-[#3DFFC4]/30 shadow-[0_0_12px_rgba(61,255,196,0.2)]'
                : 'bg-white/[0.04] text-[#93A1AF] border border-white/10'
            }`}
          >
            {running ? <Shield className="w-5 h-5" /> : <ShieldOff className="w-5 h-5" />}
          </div>

          <div className="min-w-0 space-y-0.5">
            <h3 className="text-sm font-bold text-white group-hover:text-[#3DFFC4] transition-colors">
              {running ? t.toggle_shield_on : t.toggle_shield_off}
            </h3>
            <p className="text-xs text-[#93A1AF] truncate">
              {t.toggle_hint}
            </p>
          </div>
        </div>

        {/* Tactile Capsule Switch */}
        <div
          className={`relative w-14 h-8 rounded-full p-1 transition-all duration-300 shrink-0 border ${
            running
              ? 'bg-[#3DFFC4] border-[#3DFFC4] shadow-[0_0_15px_rgba(61,255,196,0.4)]'
              : 'bg-[#141B24] border-white/15'
          }`}
        >
          <div
            className={`w-6 h-6 rounded-full shadow-md transition-all duration-300 ease-spring flex items-center justify-center transform ${
              running ? 'translate-x-6 bg-[#03261C] text-[#3DFFC4]' : 'translate-x-0 bg-[#93A1AF] text-[#141B24]'
            }`}
          >
            <Power className="w-3 h-3 stroke-[3]" />
          </div>
        </div>
      </div>
    </GlassCard>
  );
};
