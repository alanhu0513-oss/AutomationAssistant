import React from 'react';
import { AlertCircle, ArrowRight } from 'lucide-react';
import { GlassCard } from './GlassCard';
import { translations, Locale } from '../i18n/translations';

interface OfflineBannerProps {
  locale: Locale;
  onReenable: () => void;
}

export const OfflineBanner: React.FC<OfflineBannerProps> = ({ locale, onReenable }) => {
  const t = translations[locale];

  return (
    <GlassCard borderAccent="red" className="p-4">
      <div className="flex items-center gap-3.5">
        <div className="flex items-center justify-center w-8 h-8 rounded-full bg-[#FF5C5C]/20 text-[#FF5C5C] shrink-0 border border-[#FF5C5C]/30">
          <AlertCircle className="w-4 h-4" />
        </div>

        <div className="flex-1 min-w-0 space-y-0.5">
          <h4 className="text-xs font-bold text-[#FF5C5C] uppercase tracking-wide">
            {t.offline_title}
          </h4>
          <p className="text-xs text-[#93A1AF] leading-tight line-clamp-2">
            {t.offline_body}
          </p>
        </div>

        <button
          onClick={onReenable}
          className="shrink-0 px-3 py-1.5 rounded-xl bg-[#FF5C5C]/15 hover:bg-[#FF5C5C]/25 text-[#FF5C5C] border border-[#FF5C5C]/30 text-xs font-bold transition-colors flex items-center gap-1"
        >
          <span>{t.offline_action}</span>
          <ArrowRight className="w-3 h-3" />
        </button>
      </div>
    </GlassCard>
  );
};
