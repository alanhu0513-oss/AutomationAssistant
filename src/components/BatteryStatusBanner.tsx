import React from 'react';
import { BatteryCharging, AlertTriangle, CheckCircle2, ChevronRight, Wrench } from 'lucide-react';
import { BatteryOptStatus } from '../automation/BatteryOptManager';
import { Locale, translations } from '../i18n/translations';

interface BatteryStatusBannerProps {
  status: BatteryOptStatus;
  locale: Locale;
  onOpenQuickFix: () => void;
}

export const BatteryStatusBanner: React.FC<BatteryStatusBannerProps> = ({
  status,
  locale,
  onOpenQuickFix,
}) => {
  const t = translations[locale];
  const isExempt = status === 'exempt';

  return (
    <div
      onClick={onOpenQuickFix}
      className={`p-4 rounded-2xl border transition-all duration-300 cursor-pointer group flex items-center justify-between gap-4 ${
        isExempt
          ? 'bg-[#3DFFC4]/[0.06] border-[#3DFFC4]/25 hover:border-[#3DFFC4]/50'
          : 'bg-[#FBBF24]/[0.08] border-[#FBBF24]/30 hover:border-[#FBBF24]/50'
      }`}
    >
      <div className="flex items-center gap-3.5 min-w-0">
        <div
          className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 border ${
            isExempt
              ? 'bg-[#3DFFC4]/15 border-[#3DFFC4]/30 text-[#3DFFC4]'
              : 'bg-[#FBBF24]/15 border-[#FBBF24]/30 text-[#FBBF24]'
          }`}
        >
          {isExempt ? <CheckCircle2 className="w-5 h-5" /> : <AlertTriangle className="w-5 h-5" />}
        </div>

        <div className="min-w-0 space-y-0.5">
          <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-white">
              {isExempt ? t.quick_fix_status_exempt : t.quick_fix_status_optimizing}
            </span>
            <span
              className={`text-[10px] font-mono font-bold px-1.5 py-0.5 rounded ${
                isExempt ? 'bg-[#3DFFC4]/20 text-[#3DFFC4]' : 'bg-[#FBBF24]/20 text-[#FBBF24]'
              }`}
            >
              {isExempt ? 'EXEMPT' : 'OPTIMIZING'}
            </span>
          </div>
          <p className="text-[11px] text-[#93A1AF] truncate">
            {isExempt
              ? (locale === 'zh' ? '电池优化白名单生效中，后台守护稳定' : 'Unrestricted background running active')
              : (locale === 'zh' ? '点击使用一键修复直达系统白名单设置' : 'Click to launch Quick Fix and exempt from battery saver')}
          </p>
        </div>
      </div>

      <button
        type="button"
        className={`px-3.5 py-2 rounded-xl text-xs font-bold flex items-center gap-1.5 shrink-0 transition-all active:scale-95 ${
          isExempt
            ? 'bg-white/[0.06] text-white hover:bg-white/[0.12] border border-white/10'
            : 'bg-[#FBBF24] hover:bg-[#FCD34D] text-[#1E1500] shadow-md shadow-[#FBBF24]/20 font-extrabold'
        }`}
      >
        <Wrench className="w-3.5 h-3.5" />
        <span>{locale === 'zh' ? '一键修复' : 'Quick Fix'}</span>
        <ChevronRight className="w-3.5 h-3.5" />
      </button>
    </div>
  );
};
