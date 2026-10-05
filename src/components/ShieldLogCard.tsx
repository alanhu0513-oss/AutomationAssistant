import React from 'react';
import { History, Trash2, ShieldCheck, Eye } from 'lucide-react';
import { GlassCard } from './GlassCard';
import { LogEntry } from '../types';
import { translations, Locale } from '../i18n/translations';

interface ShieldLogCardProps {
  entries: LogEntry[];
  locale: Locale;
  onClear: () => void;
}

export const ShieldLogCard: React.FC<ShieldLogCardProps> = ({ entries, locale, onClear }) => {
  const t = translations[locale];
  const MAX_VISIBLE = 8;

  const formatTime = (epoch: number) => {
    const d = new Date(epoch);
    const hours = d.getHours().toString().padStart(2, '0');
    const minutes = d.getMinutes().toString().padStart(2, '0');
    const seconds = d.getSeconds().toString().padStart(2, '0');
    return `${hours}:${minutes}:${seconds}`;
  };

  return (
    <div className="w-full space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <History className="w-4 h-4 text-[#2DD4BF]" />
            <span>{t.log_section_title}</span>
            <span className="text-xs font-mono text-[#93A1AF]">({entries.length})</span>
          </h3>
          <p className="text-xs text-[#93A1AF]">
            {locale === 'zh' ? '实时记录每次拦截弹窗与防抖决策' : 'Live proof-of-work activity timeline'}
          </p>
        </div>

        {entries.length > 0 && (
          <button
            onClick={onClear}
            className="text-xs font-semibold text-[#3DFFC4] hover:text-[#5EEAD4] flex items-center gap-1.5 transition-colors px-2.5 py-1.5 rounded-xl hover:bg-white/[0.06] border border-transparent hover:border-white/10"
          >
            <Trash2 className="w-3.5 h-3.5" />
            <span>{t.log_clear}</span>
          </button>
        )}
      </div>

      <GlassCard borderAccent="teal" className="p-5">
        {entries.length === 0 ? (
          <div className="py-8 text-center space-y-1.5">
            <History className="w-6 h-6 text-[#93A1AF]/40 mx-auto" />
            <p className="text-xs text-[#93A1AF]">{t.log_empty}</p>
          </div>
        ) : (
          <div className="space-y-2.5">
            {entries.slice(0, MAX_VISIBLE).map((entry, idx) => {
              const isPreview = Boolean(entry.preview);
              return (
                <div
                  key={`${entry.atEpochMillis}-${idx}`}
                  className="flex items-center justify-between p-3 rounded-xl bg-white/[0.02] border border-white/[0.04] text-xs hover:bg-white/[0.04] transition-colors"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div
                      className={`flex items-center justify-center w-7 h-7 rounded-lg shrink-0 ${
                        isPreview
                          ? 'bg-[#FBBF24]/15 text-[#FBBF24] border border-[#FBBF24]/30'
                          : 'bg-[#3DFFC4]/15 text-[#3DFFC4] border border-[#3DFFC4]/30'
                      }`}
                    >
                      {isPreview ? <Eye className="w-3.5 h-3.5" /> : <ShieldCheck className="w-3.5 h-3.5" />}
                    </div>

                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <span className="font-semibold text-white truncate">
                          {entry.overlayLabel || t.log_unknown_app}
                        </span>
                        {isPreview && (
                          <span className="text-[10px] font-mono font-bold px-1.5 py-0.5 rounded bg-[#FBBF24]/20 text-[#FBBF24]">
                            PREVIEW
                          </span>
                        )}
                      </div>
                      <span className="text-[11px] text-[#93A1AF] truncate block">
                        {t.log_entry_subtitle(entry.gameLabel || 'Game', formatTime(entry.atEpochMillis))}
                      </span>
                    </div>
                  </div>

                  <span className="text-[11px] font-mono text-[#93A1AF] shrink-0 ml-2">
                    {formatTime(entry.atEpochMillis)}
                  </span>
                </div>
              );
            })}

            {entries.length > MAX_VISIBLE && (
              <div className="pt-2 text-center text-xs text-[#93A1AF] border-t border-white/[0.06]">
                {t.log_overflow(entries.length - MAX_VISIBLE)}
              </div>
            )}
          </div>
        )}
      </GlassCard>
    </div>
  );
};
