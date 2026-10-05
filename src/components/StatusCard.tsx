import React from 'react';
import { ShieldCheck, ShieldAlert, Power, ChevronRight, Activity, Zap } from 'lucide-react';
import { GlassCard } from './GlassCard';
import { translations, Locale } from '../i18n/translations';

interface StatusCardProps {
  isRunning: boolean;
  selectedGameCount: number;
  blockedCount: number;
  locale: Locale;
  onKeepRunning: () => void;
  onToggleShield: () => void;
}

export const StatusCard: React.FC<StatusCardProps> = ({
  isRunning,
  selectedGameCount,
  blockedCount,
  locale,
  onKeepRunning,
  onToggleShield,
}) => {
  const t = translations[locale];

  // Visual state computation
  const isArmed = isRunning && selectedGameCount > 0;
  const isWaitingGames = isRunning && selectedGameCount === 0;

  const getThemeColor = () => {
    if (!isRunning) return { text: 'text-[#FF5C5C]', border: 'border-[#FF5C5C]/30', glow: 'from-[#FF5C5C]/15', accent: 'red' as const, bg: 'bg-[#FF5C5C]/10' };
    if (isWaitingGames) return { text: 'text-[#FBBF24]', border: 'border-[#FBBF24]/30', glow: 'from-[#FBBF24]/15', accent: 'amber' as const, bg: 'bg-[#FBBF24]/10' };
    return { text: 'text-[#3DFFC4]', border: 'border-[#3DFFC4]/30', glow: 'from-[#3DFFC4]/15', accent: 'green' as const, bg: 'bg-[#3DFFC4]/10' };
  };

  const theme = getThemeColor();

  const getTitle = () => {
    if (!isRunning) return t.status_standby_title;
    if (isWaitingGames) return t.status_waiting_title;
    return t.status_operational_title;
  };

  const getBody = () => {
    if (!isRunning) return t.status_standby_hint;
    if (isWaitingGames) return t.status_waiting_hint;
    return t.status_operational_hint;
  };

  return (
    <GlassCard borderAccent={theme.accent} className="p-6 sm:p-8 overflow-hidden relative group">
      {/* Background Ambient Glow */}
      <div
        className={`absolute -top-24 -right-24 w-72 h-72 rounded-full bg-gradient-to-br ${theme.glow} to-transparent blur-3xl pointer-events-none transition-all duration-700 ${
          isArmed ? 'animate-breathe' : 'opacity-40'
        }`}
      />

      <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
        {/* Left Status Area */}
        <div className="flex items-start sm:items-center gap-4 sm:gap-5">
          {/* Animated Emblem */}
          <div className="relative shrink-0">
            <div
              className={`w-16 h-16 sm:w-20 sm:h-20 rounded-2xl ${theme.bg} border ${theme.border} flex items-center justify-center transition-all duration-500 relative`}
            >
              {isArmed ? (
                <>
                  <span className="absolute inset-0 rounded-2xl bg-[#3DFFC4]/20 animate-ripple pointer-events-none" />
                  <ShieldCheck className="w-8 h-8 sm:w-10 sm:h-10 text-[#3DFFC4] transition-transform duration-300 group-hover:scale-110" />
                </>
              ) : isWaitingGames ? (
                <ShieldAlert className="w-8 h-8 sm:w-10 sm:h-10 text-[#FBBF24] transition-transform duration-300" />
              ) : (
                <Power className="w-8 h-8 sm:w-10 sm:h-10 text-[#FF5C5C] transition-transform duration-300" />
              )}
            </div>
          </div>

          {/* Status Text Details */}
          <div className="space-y-1.5 min-w-0">
            <div className="flex items-center gap-2 text-xs text-[#93A1AF]">
              <Activity className="w-3.5 h-3.5" />
              <span>
                {isRunning ? (locale === 'zh' ? '无障碍监控引擎' : 'Accessibility Service') : (locale === 'zh' ? '监控已暂停' : 'Service Paused')}
              </span>
              <span aria-hidden="true">·</span>
              <span className={`font-semibold ${theme.text}`}>
                {isRunning ? (locale === 'zh' ? '运行中' : 'Active') : (locale === 'zh' ? '待命' : 'Standby')}
              </span>
            </div>

            <h2 className="text-xl sm:text-2xl font-extrabold text-white tracking-tight">
              {getTitle()}
            </h2>

            <p className="text-xs sm:text-sm text-[#93A1AF] leading-relaxed max-w-md">
              {getBody()}
            </p>
          </div>
        </div>

        {/* Right Metrics & Quick Switch */}
        <div className="flex sm:flex-col md:items-end justify-between items-center gap-4 pt-4 md:pt-0 border-t md:border-t-0 border-white/[0.06]">
          <div className="flex items-center gap-6 sm:gap-4">
            <div className="text-left md:text-right">
              <span className="text-[11px] text-[#93A1AF] block font-medium">
                {locale === 'zh' ? '守护游戏' : 'Protected'}
              </span>
              <span className="text-xl sm:text-2xl font-mono font-extrabold text-white tabular-nums">
                {selectedGameCount}
              </span>
            </div>

            <div className="h-8 w-px bg-white/[0.08]" />

            <div className="text-left md:text-right">
              <span className="text-[11px] text-[#93A1AF] block font-medium">
                {locale === 'zh' ? '拦截弹窗' : 'Dismissals'}
              </span>
              <span className="text-xl sm:text-2xl font-mono font-extrabold text-[#3DFFC4] tabular-nums">
                {blockedCount}
              </span>
            </div>
          </div>

          <button
            onClick={onToggleShield}
            className={`px-4 py-2 rounded-xl text-xs font-bold transition-all duration-300 flex items-center gap-2 active:scale-95 shadow-md ${
              isRunning
                ? 'bg-white/[0.06] hover:bg-white/[0.1] text-white/90 border border-white/10'
                : 'bg-[#3DFFC4] hover:bg-[#5EEAD4] text-[#03261C] shadow-[0_0_15px_rgba(61,255,196,0.35)]'
            }`}
          >
            <Power className="w-3.5 h-3.5" />
            <span>{isRunning ? (locale === 'zh' ? '暂停护盾' : 'Pause Shield') : (locale === 'zh' ? '启动护盾' : 'Activate Shield')}</span>
          </button>
        </div>
      </div>

      {/* Low-profile keep-alive helper link */}
      <div className="mt-5 pt-4 border-t border-white/[0.06] flex items-center justify-between text-xs text-[#93A1AF]">
        <span className="flex items-center gap-1.5">
          <Zap className="w-3.5 h-3.5 text-[#FBBF24]" />
          {locale === 'zh' ? '确保后台持续运行：需关闭电池智能优化' : 'Battery background optimization must be exempt'}
        </span>
        <button
          onClick={onKeepRunning}
          className="text-[#3DFFC4] hover:underline font-semibold flex items-center gap-0.5 shrink-0"
        >
          <span>{t.keep_running}</span>
          <ChevronRight className="w-3.5 h-3.5" />
        </button>
      </div>
    </GlassCard>
  );
};
