import React from 'react';
import { CheckCircle2, AlertTriangle, HelpCircle } from 'lucide-react';
import { GlassCard } from './GlassCard';
import { translations, Locale } from '../i18n/translations';

interface SystemCapabilitiesCardProps {
  locale: Locale;
}

export const SystemCapabilitiesCard: React.FC<SystemCapabilitiesCardProps> = ({ locale }) => {
  const t = translations[locale];

  return (
    <div className="w-full space-y-4">
      <div>
        <h3 className="text-base font-bold text-white">{t.capabilities_section_title}</h3>
        <p className="text-xs text-[#93A1AF]">
          {locale === 'zh' ? '透明公开的技术边界与系统安全原则' : 'Transparent technical principles and security boundaries'}
        </p>
      </div>

      <GlassCard borderAccent="default" className="p-6 space-y-5">
        {/* What It Does Perfectly */}
        <div className="flex items-start gap-3.5">
          <div className="flex items-center justify-center w-8 h-8 rounded-xl bg-[#3DFFC4]/15 text-[#3DFFC4] shrink-0 mt-0.5 border border-[#3DFFC4]/30">
            <CheckCircle2 className="w-4 h-4" />
          </div>
          <div className="space-y-1 min-w-0">
            <h4 className="text-xs font-bold text-[#3DFFC4] tracking-wide">
              {t.capability_perfect_label}
            </h4>
            <p className="text-xs text-[#93A1AF] leading-relaxed">
              {t.capability_perfect_body}
            </p>
          </div>
        </div>

        <div className="h-px w-full bg-white/[0.06]" />

        {/* What It Cannot Do */}
        <div className="flex items-start gap-3.5">
          <div className="flex items-center justify-center w-8 h-8 rounded-xl bg-[#FF5C5C]/15 text-[#FF5C5C] shrink-0 mt-0.5 border border-[#FF5C5C]/30">
            <AlertTriangle className="w-4 h-4" />
          </div>
          <div className="space-y-1 min-w-0">
            <h4 className="text-xs font-bold text-[#FF5C5C] tracking-wide">
              {t.capability_limit_label}
            </h4>
            <p className="text-xs text-[#93A1AF] leading-relaxed">
              {t.capability_limit_body}
            </p>
          </div>
        </div>

        <div className="h-px w-full bg-white/[0.06]" />

        {/* The 1% Rule */}
        <div className="flex items-start gap-3.5">
          <div className="flex items-center justify-center w-8 h-8 rounded-xl bg-[#FBBF24]/15 text-[#FBBF24] shrink-0 mt-0.5 border border-[#FBBF24]/30">
            <HelpCircle className="w-4 h-4" />
          </div>
          <div className="space-y-1 min-w-0">
            <h4 className="text-xs font-bold text-[#FBBF24] tracking-wide">
              {t.capability_slow_label}
            </h4>
            <p className="text-xs text-[#93A1AF] leading-relaxed">
              {t.capability_slow_body}
            </p>
          </div>
        </div>
      </GlassCard>
    </div>
  );
};
