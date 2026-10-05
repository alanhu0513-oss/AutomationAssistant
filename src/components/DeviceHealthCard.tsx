import React from 'react';
import { BatteryCharging, ChevronRight, Smartphone, Wrench } from 'lucide-react';
import { GlassCard } from './GlassCard';
import { OemBrand } from '../types';
import { OemGuide } from '../data/OemGuide';
import { translations, Locale } from '../i18n/translations';

interface DeviceHealthCardProps {
  selectedBrand: OemBrand;
  locale: Locale;
  onSelectBrand: (brand: OemBrand) => void;
  onOpenBatterySettings: () => void;
  onOpenQuickFix?: () => void;
}

export const DeviceHealthCard: React.FC<DeviceHealthCardProps> = ({
  selectedBrand,
  locale,
  onSelectBrand,
  onOpenBatterySettings,
  onOpenQuickFix,
}) => {
  const t = translations[locale];
  const brands = OemGuide.getAllBrands();
  const stepKeys = OemGuide.getStepsForBrand(selectedBrand);

  const getBrandTitle = (brand: OemBrand) => {
    switch (brand) {
      case OemBrand.MIUI:
        return t.brand_miui;
      case OemBrand.COLOROS:
        return t.brand_coloros;
      case OemBrand.ONEUI:
        return t.brand_oneui;
      case OemBrand.FUNTOUCH:
        return t.brand_funtouch;
      case OemBrand.STOCK:
      default:
        return t.brand_stock;
    }
  };

  const getStepText = (key: string) => {
    return (t as any)[key] || key;
  };

  return (
    <div className="w-full space-y-4">
      {/* Header & Brand Segmented Buttons */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <BatteryCharging className="w-4 h-4 text-[#FBBF24]" />
            <span>{t.health_title}</span>
          </h3>
          <p className="text-xs text-[#93A1AF]">
            {locale === 'zh' ? '防止后台进程被各厂商激进的省电机制清理' : 'Prevent background service from being killed by aggressive OEM battery managers'}
          </p>
        </div>

        {/* Brand Segmented Selector */}
        <div className="flex items-center gap-1.5 p-1 bg-[#0F141D] rounded-xl border border-white/[0.08] overflow-x-auto">
          {brands.map((b) => {
            const isSelected = selectedBrand === b.brand;
            return (
              <button
                key={b.brand}
                onClick={() => onSelectBrand(b.brand)}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium whitespace-nowrap transition-all ${
                  isSelected
                    ? 'bg-[#FBBF24]/15 text-[#FBBF24] font-semibold border border-[#FBBF24]/30 shadow-sm'
                    : 'text-[#93A1AF] hover:text-white'
                }`}
              >
                {b.brand}
              </button>
            );
          })}
        </div>
      </div>

      <GlassCard borderAccent="amber" className="p-6 space-y-5">
        <div className="flex items-center justify-between gap-4">
          <div className="flex items-center gap-2.5 text-xs font-semibold text-[#FBBF24]">
            <Smartphone className="w-4 h-4 shrink-0" />
            <span>{t.health_detected(getBrandTitle(selectedBrand))}</span>
          </div>

          {onOpenQuickFix && (
            <button
              onClick={onOpenQuickFix}
              className="px-3 py-1 rounded-xl bg-[#FBBF24] hover:bg-[#FCD34D] text-[#1E1500] font-bold text-xs flex items-center gap-1.5 active:scale-95 transition-all shrink-0"
            >
              <Wrench className="w-3.5 h-3.5" />
              <span>{locale === 'zh' ? '一键修复' : 'Quick Fix'}</span>
            </button>
          )}
        </div>

        <p className="text-xs text-[#93A1AF] leading-relaxed">{t.health_intro}</p>

        {/* Step-by-Step Instructions */}
        <div className="space-y-2.5">
          {stepKeys.map((stepKey, idx) => (
            <div
              key={idx}
              className="flex items-start gap-3 p-3 rounded-xl bg-white/[0.02] border border-white/[0.05]"
            >
              <span className="flex items-center justify-center w-5 h-5 rounded-lg bg-[#FBBF24]/15 text-[#FBBF24] font-bold text-[11px] shrink-0 mt-0.5 border border-[#FBBF24]/30">
                {idx + 1}
              </span>
              <span className="text-xs text-white/90 leading-relaxed pt-0.5">
                {getStepText(stepKey)}
              </span>
            </div>
          ))}
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-2">
          {onOpenQuickFix && (
            <button
              onClick={onOpenQuickFix}
              className="py-2.5 px-4 rounded-xl bg-[#3DFFC4] hover:bg-[#5EEAD4] text-xs font-bold text-[#03261C] flex items-center justify-center gap-2 active:scale-98 transition-all shadow-md"
            >
              <Wrench className="w-3.5 h-3.5" />
              <span>{t.quick_fix_title}</span>
            </button>
          )}

          <button
            onClick={onOpenBatterySettings}
            className="py-2.5 px-4 rounded-xl bg-white/[0.04] hover:bg-[#FBBF24]/10 text-xs font-semibold text-[#FBBF24] border border-[#FBBF24]/20 hover:border-[#FBBF24]/40 transition-all duration-200 flex items-center justify-center gap-2 active:scale-98"
          >
            <span>{t.health_open_battery}</span>
            <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>
      </GlassCard>
    </div>
  );
};
