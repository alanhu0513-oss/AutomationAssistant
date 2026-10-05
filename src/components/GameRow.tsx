import React from 'react';
import { Shield, Zap, Check } from 'lucide-react';
import { AppEntry, StrictnessLevel } from '../types';
import { translations, Locale } from '../i18n/translations';

interface GameRowProps {
  entry: AppEntry;
  isProtected: boolean;
  strictness: StrictnessLevel;
  locale: Locale;
  onToggleProtected: () => void;
  onCycleStrictness: (e: React.MouseEvent) => void;
}

export const GameRow: React.FC<GameRowProps> = ({
  entry,
  isProtected,
  strictness,
  locale,
  onToggleProtected,
  onCycleStrictness,
}) => {
  const t = translations[locale];

  const getStrictnessStyle = () => {
    switch (strictness) {
      case 'normal':
        return 'text-[#2DD4BF] bg-[#2DD4BF]/10 border-[#2DD4BF]/30 hover:bg-[#2DD4BF]/20';
      case 'gentle':
        return 'text-[#3DFFC4] bg-[#3DFFC4]/10 border-[#3DFFC4]/30 hover:bg-[#3DFFC4]/20';
      case 'strict':
        return 'text-[#FBBF24] bg-[#FBBF24]/10 border-[#FBBF24]/30 hover:bg-[#FBBF24]/20';
    }
  };

  const getStrictnessLabel = () => {
    switch (strictness) {
      case 'normal':
        return t.strictness_normal;
      case 'gentle':
        return t.strictness_gentle;
      case 'strict':
        return t.strictness_strict;
    }
  };

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .slice(0, 2)
      .map((w) => w[0])
      .join('')
      .toUpperCase();
  };

  return (
    <div
      onClick={onToggleProtected}
      className={`group relative flex items-center justify-between p-3.5 rounded-2xl cursor-pointer transition-all duration-200 border ${
        isProtected
          ? 'bg-white/[0.05] border-[#3DFFC4]/35 shadow-[0_4px_20px_rgba(61,255,196,0.06)]'
          : 'bg-white/[0.015] border-white/[0.06] hover:bg-white/[0.035] hover:border-white/15'
      }`}
    >
      <div className="flex items-center gap-3.5 min-w-0">
        {/* App Icon or Initials Avatar */}
        <div
          className={`w-11 h-11 rounded-xl flex items-center justify-center font-bold text-xs shrink-0 transition-colors ${
            isProtected
              ? 'bg-[#3DFFC4]/15 text-[#3DFFC4] border border-[#3DFFC4]/30'
              : 'bg-[#121820] text-[#93A1AF] border border-white/[0.08]'
          }`}
        >
          {entry.iconUrl ? (
            <img
              src={entry.iconUrl}
              alt={entry.label}
              className="w-full h-full rounded-xl object-cover"
            />
          ) : (
            getInitials(entry.label)
          )}
        </div>

        {/* Title and Package Info */}
        <div className="min-w-0 space-y-0.5">
          <h4
            className={`text-sm font-semibold truncate transition-colors ${
              isProtected ? 'text-white font-bold' : 'text-white/85 group-hover:text-white'
            }`}
          >
            {entry.label}
          </h4>
          <p className="text-[11px] text-[#93A1AF] font-mono truncate">
            {entry.packageName}
          </p>
        </div>
      </div>

      {/* Right Controls */}
      <div className="flex items-center gap-2.5 shrink-0 ml-3">
        {isProtected && (
          <button
            type="button"
            onClick={onCycleStrictness}
            title={locale === 'zh' ? '点击切换防抖响应级别' : 'Click to cycle debounce level'}
            className={`px-2.5 py-1 rounded-lg text-[11px] font-semibold border flex items-center gap-1 transition-all duration-150 active:scale-95 ${getStrictnessStyle()}`}
          >
            <Zap className="w-3 h-3 shrink-0" />
            <span className="whitespace-nowrap">{getStrictnessLabel()}</span>
          </button>
        )}

        {/* Selection Check Circle */}
        <div
          className={`w-7 h-7 rounded-xl flex items-center justify-center transition-all duration-200 ${
            isProtected
              ? 'bg-[#3DFFC4] text-[#03261C] shadow-[0_0_10px_rgba(61,255,196,0.4)]'
              : 'border border-white/15 text-transparent group-hover:border-white/30'
          }`}
        >
          <Check className="w-4 h-4 stroke-[3]" />
        </div>
      </div>
    </div>
  );
};
