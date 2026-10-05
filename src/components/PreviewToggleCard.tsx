import React from 'react';
import { Eye } from 'lucide-react';
import { GlassCard } from './GlassCard';
import { translations, Locale } from '../i18n/translations';

interface PreviewToggleCardProps {
  enabled: boolean;
  locale: Locale;
  onToggle: (enabled: boolean) => void;
}

export const PreviewToggleCard: React.FC<PreviewToggleCardProps> = ({ enabled, locale, onToggle }) => {
  const t = translations[locale];

  return (
    <GlassCard borderAccent={enabled ? 'amber' : 'teal'} className="p-5">
      <div className="flex items-center justify-between gap-4">
        <div className="flex-1 space-y-1">
          <div className="flex items-center gap-2">
            <Eye className={`w-4 h-4 ${enabled ? 'text-[#FBBF24]' : 'text-[#2DD4BF]'}`} />
            <h4
              className={`text-sm font-semibold tracking-wide transition-colors ${
                enabled ? 'text-[#FBBF24]' : 'text-white'
              }`}
            >
              {t.preview_title}
            </h4>
          </div>
          <p className="text-xs text-[#93A1AF] leading-relaxed">{t.preview_body}</p>
        </div>

        {/* Small Toggle Switch */}
        <button
          onClick={() => onToggle(!enabled)}
          className={`relative w-12 h-7 rounded-full p-1 transition-all duration-300 shrink-0 focus:outline-none focus:ring-2 focus:ring-[#FBBF24]/40 ${
            enabled ? 'bg-[#FBBF24]' : 'bg-[#1F2833] border border-white/10'
          }`}
        >
          <div
            className={`w-5 h-5 rounded-full shadow-md transition-all duration-300 transform ${
              enabled ? 'translate-x-5 bg-[#03261C]' : 'translate-x-0 bg-[#93A1AF]'
            }`}
          />
        </button>
      </div>
    </GlassCard>
  );
};
