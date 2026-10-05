import React from 'react';
import { Shield, Globe, RotateCcw, Download } from 'lucide-react';
import { Locale } from '../i18n/translations';

export type NavTab = 'shield' | 'games' | 'simulator' | 'guide';

interface HeaderProps {
  currentTab: NavTab;
  locale: Locale;
  isRunning: boolean;
  onSelectTab: (tab: NavTab) => void;
  onToggleLocale: () => void;
  onReplayOnboarding: () => void;
  onOpenDownloadApk: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  currentTab,
  locale,
  isRunning,
  onSelectTab,
  onToggleLocale,
  onReplayOnboarding,
  onOpenDownloadApk,
}) => {
  const tabs: { id: NavTab; labelEn: string; labelZh: string }[] = [
    { id: 'shield', labelEn: 'Shield', labelZh: '护盾中心' },
    { id: 'games', labelEn: 'Games', labelZh: '游戏守护' },
    { id: 'simulator', labelEn: 'Lab Simulator', labelZh: '模拟实验室' },
    { id: 'guide', labelEn: 'Device Guide', labelZh: '设备指南' },
  ];

  return (
    <header className="sticky top-0 z-40 w-full backdrop-blur-2xl bg-[#07090E]/80 border-b border-white/[0.06] transition-all">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between gap-4">
        {/* Zone 1: Brand Wordmark */}
        <button
          onClick={() => onSelectTab('shield')}
          className="flex items-center gap-2.5 text-left group focus:outline-none shrink-0"
        >
          <div className="relative flex items-center justify-center w-8 h-8 rounded-xl bg-gradient-to-tr from-[#3DFFC4]/20 to-[#2DD4BF]/5 border border-[#3DFFC4]/30 shadow-[0_0_15px_rgba(61,255,196,0.15)] group-hover:scale-105 transition-transform duration-300">
            <Shield className="w-4 h-4 text-[#3DFFC4]" />
            {isRunning && (
              <span className="absolute -top-0.5 -right-0.5 w-2 h-2 rounded-full bg-[#3DFFC4] shadow-[0_0_6px_#3DFFC4]" />
            )}
          </div>
          <span className="text-base font-bold tracking-tight text-white group-hover:text-[#3DFFC4] transition-colors">
            Aegis
          </span>
        </button>

        {/* Zone 2: Navigation Links / Segmented Tabs */}
        <nav className="hidden sm:flex items-center p-1 rounded-xl bg-white/[0.03] border border-white/[0.06]">
          {tabs.map((tab) => {
            const isActive = currentTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => onSelectTab(tab.id)}
                className={`px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all duration-200 whitespace-nowrap ${
                  isActive
                    ? 'bg-white/[0.1] text-white font-semibold shadow-sm shadow-black/40 border border-white/[0.08]'
                    : 'text-[#93A1AF] hover:text-white hover:bg-white/[0.03]'
                }`}
              >
                {locale === 'zh' ? tab.labelZh : tab.labelEn}
              </button>
            );
          })}
        </nav>

        {/* Zone 3: Primary Actions (Download APK + Locale + Replay) */}
        <div className="flex items-center gap-2 shrink-0">
          <button
            onClick={onOpenDownloadApk}
            className="px-3 py-1.5 rounded-xl bg-[#3DFFC4] hover:bg-[#5EEAD4] text-[#03261C] font-extrabold text-xs flex items-center gap-1.5 shadow-md shadow-[#3DFFC4]/20 active:scale-95 transition-all"
          >
            <Download className="w-3.5 h-3.5 stroke-[2.5]" />
            <span className="hidden xs:inline">{locale === 'zh' ? '下载 APK' : 'Get APK'}</span>
          </button>

          <button
            onClick={onReplayOnboarding}
            title={locale === 'zh' ? '重新播放向导' : 'Replay Onboarding Guide'}
            className="p-2 rounded-xl text-[#93A1AF] hover:text-white hover:bg-white/[0.06] border border-transparent hover:border-white/[0.08] transition-all text-xs flex items-center gap-1.5 active:scale-95"
          >
            <RotateCcw className="w-3.5 h-3.5" />
          </button>

          <button
            onClick={onToggleLocale}
            className="px-2.5 py-1.5 rounded-xl bg-white/[0.04] hover:bg-[#3DFFC4]/15 text-[#3DFFC4] border border-[#3DFFC4]/25 hover:border-[#3DFFC4]/50 text-xs font-semibold transition-all flex items-center gap-1.5 active:scale-95 shadow-sm"
          >
            <Globe className="w-3.5 h-3.5" />
            <span>{locale === 'en' ? '中文' : 'EN'}</span>
          </button>
        </div>
      </div>

      {/* Mobile Secondary Tab Bar */}
      <div className="sm:hidden flex items-center justify-around px-2 py-1.5 border-t border-white/[0.04] bg-[#0A0E17]/60">
        {tabs.map((tab) => {
          const isActive = currentTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => onSelectTab(tab.id)}
              className={`px-3 py-1 text-[11px] font-medium transition-all rounded-md ${
                isActive
                  ? 'text-[#3DFFC4] font-bold bg-[#3DFFC4]/10'
                  : 'text-[#93A1AF] hover:text-white'
              }`}
            >
              {locale === 'zh' ? tab.labelZh : tab.labelEn}
            </button>
          );
        })}
      </div>
    </header>
  );
};
