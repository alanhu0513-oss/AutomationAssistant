import React, { useState } from 'react';
import { Shield, Settings, Lock, ArrowRight, Check } from 'lucide-react';
import { GlassCard } from './GlassCard';
import { translations, Locale } from '../i18n/translations';

interface OnboardingProps {
  locale: Locale;
  onOpenAccessibilitySettings: () => void;
  onFinished: () => void;
}

export const Onboarding: React.FC<OnboardingProps> = ({
  locale,
  onOpenAccessibilitySettings,
  onFinished,
}) => {
  const t = translations[locale];
  const [currentSlide, setCurrentSlide] = useState(0);

  const slides = [
    {
      icon: <Shield className="w-12 h-12 text-[#3DFFC4]" />,
      title: t.slide1_title,
      body: t.slide1_body,
    },
    {
      icon: <Settings className="w-12 h-12 text-[#3DFFC4]" />,
      title: t.slide2_title,
      body: t.slide2_body,
    },
    {
      icon: <Lock className="w-12 h-12 text-[#3DFFC4]" />,
      title: t.slide3_title,
      body: t.slide3_body,
    },
  ];

  return (
    <div className="min-h-screen bg-[#080B10] flex flex-col justify-between p-6 max-w-lg mx-auto animate-fade-in select-none">
      {/* Top Header */}
      <div className="flex items-center justify-between pt-4">
        <span className="text-xs font-bold uppercase tracking-widest text-[#3DFFC4]">
          Aegis Setup · {currentSlide + 1} / 3
        </span>
        <button
          onClick={onFinished}
          className="text-xs text-[#93A1AF] hover:text-white transition-colors"
        >
          Skip
        </button>
      </div>

      {/* Main Slide Content */}
      <div className="flex-1 flex flex-col items-center justify-center my-8 text-center space-y-8">
        <GlassCard
          borderAccent="green"
          className="w-28 h-28 flex items-center justify-center shadow-[0_0_30px_rgba(61,255,196,0.2)]"
        >
          {slides[currentSlide].icon}
        </GlassCard>

        <div className="space-y-3 max-w-sm">
          <h2 className="text-2xl font-extrabold text-white tracking-tight">
            {slides[currentSlide].title}
          </h2>
          <p className="text-sm text-[#93A1AF] leading-relaxed">
            {slides[currentSlide].body}
          </p>
        </div>
      </div>

      {/* Footer Controls */}
      <div className="space-y-6 pb-6">
        {/* Neon Pager Dots */}
        <div className="flex items-center justify-center gap-2">
          {slides.map((_, idx) => (
            <button
              key={idx}
              onClick={() => setCurrentSlide(idx)}
              className={`h-2 rounded-full transition-all duration-300 ${
                idx === currentSlide
                  ? 'w-7 bg-[#3DFFC4] shadow-[0_0_10px_rgba(61,255,196,0.6)]'
                  : 'w-2 bg-[#2A3542]'
              }`}
            />
          ))}
        </div>

        {/* Dynamic Slide Action Button */}
        {currentSlide === 0 && (
          <button
            onClick={() => setCurrentSlide(1)}
            className="w-full h-14 rounded-2xl bg-[#3DFFC4] text-[#03261C] font-bold text-base hover:bg-[#5EEAD4] transition-all flex items-center justify-center gap-2 shadow-[0_0_20px_rgba(61,255,196,0.3)]"
          >
            <span>{t.slide1_next}</span>
            <ArrowRight className="w-5 h-5" />
          </button>
        )}

        {currentSlide === 1 && (
          <div className="space-y-2">
            <button
              onClick={() => {
                onOpenAccessibilitySettings();
                setCurrentSlide(2);
              }}
              className="w-full h-14 rounded-2xl bg-[#3DFFC4] text-[#03261C] font-bold text-base hover:bg-[#5EEAD4] transition-all flex items-center justify-center gap-2 shadow-[0_0_20px_rgba(61,255,196,0.3)]"
            >
              <Settings className="w-5 h-5" />
              <span>{t.slide2_grant}</span>
            </button>
            <button
              onClick={() => setCurrentSlide(2)}
              className="w-full py-2 text-xs font-semibold text-[#93A1AF] hover:text-white"
            >
              {t.slide2_next}
            </button>
          </div>
        )}

        {currentSlide === 2 && (
          <button
            onClick={onFinished}
            className="w-full h-14 rounded-2xl bg-gradient-to-r from-[#3DFFC4] to-[#2DD4BF] text-[#03261C] font-extrabold text-base hover:opacity-95 transition-all flex items-center justify-center gap-2 shadow-[0_0_25px_rgba(61,255,196,0.4)]"
          >
            <span>{t.slide3_finish}</span>
            <Check className="w-5 h-5" />
          </button>
        )}
      </div>
    </div>
  );
};
