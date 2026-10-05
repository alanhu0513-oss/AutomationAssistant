import React, { useState } from 'react';
import { Search, Plus, Gamepad2, Loader2, CheckSquare, Square, X } from 'lucide-react';
import { GameRow } from './GameRow';
import { AppEntry, StrictnessLevel } from '../types';
import { AppSearch } from '../data/AppSearch';
import { translations, Locale } from '../i18n/translations';

interface GamesSectionProps {
  apps: AppEntry[];
  loading: boolean;
  protectedApps: Set<string>;
  strictnessLevels: Map<string, StrictnessLevel>;
  locale: Locale;
  onToggleProtected: (packageName: string, isProtected: boolean) => void;
  onCycleStrictness: (packageName: string) => void;
  onAddCustomApp: (name: string, packageName: string) => void;
}

export const GamesSection: React.FC<GamesSectionProps> = ({
  apps,
  loading,
  protectedApps,
  strictnessLevels,
  locale,
  onToggleProtected,
  onCycleStrictness,
  onAddCustomApp,
}) => {
  const t = translations[locale];
  const [query, setQuery] = useState('');
  const [filterMode, setFilterMode] = useState<'all' | 'protected'>('all');
  const [showAddModal, setShowAddModal] = useState(false);
  const [customName, setCustomName] = useState('');
  const [customPackage, setCustomPackage] = useState('');

  let filteredApps = AppSearch.filter(apps, query);
  if (filterMode === 'protected') {
    filteredApps = filteredApps.filter((app) => protectedApps.has(app.packageName));
  }

  const handleAddSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!customName.trim()) return;
    onAddCustomApp(customName, customPackage);
    setCustomName('');
    setCustomPackage('');
    setShowAddModal(false);
  };

  const handleToggleAll = () => {
    const allProtected = filteredApps.every((a) => protectedApps.has(a.packageName));
    filteredApps.forEach((app) => {
      onToggleProtected(app.packageName, !allProtected);
    });
  };

  return (
    <div className="w-full space-y-4">
      {/* Section Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Gamepad2 className="w-4 h-4 text-[#3DFFC4]" />
            <span>{t.games_section_title}</span>
            <span className="text-xs font-mono text-[#93A1AF]">({protectedApps.size} / {apps.length})</span>
          </h3>
          <p className="text-xs text-[#93A1AF]">
            {locale === 'zh' ? '勾选需要开启全屏防弹窗守护的游戏或应用' : 'Select games to automatically dismiss background overlays'}
          </p>
        </div>

        <div className="flex items-center gap-2">
          {/* Quick toggle all */}
          {filteredApps.length > 0 && (
            <button
              onClick={handleToggleAll}
              className="px-3 py-1.5 text-xs font-medium text-[#93A1AF] hover:text-white bg-white/[0.03] hover:bg-white/[0.08] border border-white/[0.08] rounded-xl transition-all"
            >
              {filteredApps.every((a) => protectedApps.has(a.packageName))
                ? (locale === 'zh' ? '取消全选' : 'Deselect All')
                : (locale === 'zh' ? '全选本页' : 'Select All')}
            </button>
          )}

          {/* Add custom game */}
          <button
            onClick={() => setShowAddModal(true)}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold text-[#03261C] bg-[#3DFFC4] hover:bg-[#5EEAD4] rounded-xl transition-all shadow-sm active:scale-95"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>{t.simulator_custom_app}</span>
          </button>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-2.5">
        {/* Search Input */}
        <div className="relative flex-1">
          <Search className="w-4 h-4 text-[#93A1AF] absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder={t.games_search_hint}
            className="w-full pl-10 pr-9 py-2.5 bg-[#0F141D] border border-white/[0.08] rounded-xl text-xs text-white placeholder-[#93A1AF] focus:outline-none focus:border-[#3DFFC4]/60 transition-colors"
          />
          {query && (
            <button
              onClick={() => setQuery('')}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-[#93A1AF] hover:text-white p-1"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          )}
        </div>

        {/* Segmented Filter */}
        <div className="flex items-center p-1 rounded-xl bg-[#0F141D] border border-white/[0.08] shrink-0">
          <button
            onClick={() => setFilterMode('all')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              filterMode === 'all'
                ? 'bg-white/[0.1] text-white font-semibold shadow-sm'
                : 'text-[#93A1AF] hover:text-white'
            }`}
          >
            {locale === 'zh' ? '全部' : 'All'} ({apps.length})
          </button>
          <button
            onClick={() => setFilterMode('protected')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              filterMode === 'protected'
                ? 'bg-[#3DFFC4]/15 text-[#3DFFC4] font-semibold border border-[#3DFFC4]/30'
                : 'text-[#93A1AF] hover:text-white'
            }`}
          >
            {locale === 'zh' ? '已守护' : 'Protected'} ({protectedApps.size})
          </button>
        </div>
      </div>

      {/* App List */}
      <div className="space-y-2">
        {loading ? (
          <div className="flex items-center justify-center gap-2.5 py-12 text-xs text-[#93A1AF]">
            <Loader2 className="w-4 h-4 animate-spin text-[#3DFFC4]" />
            <span>{t.games_loading}</span>
          </div>
        ) : filteredApps.length === 0 ? (
          <div className="py-12 text-center space-y-2 border border-dashed border-white/[0.08] rounded-2xl bg-white/[0.01]">
            <Gamepad2 className="w-8 h-8 text-[#93A1AF]/40 mx-auto" />
            <p className="text-xs text-[#93A1AF]">
              {query.trim() ? t.games_no_results : t.games_empty}
            </p>
          </div>
        ) : (
          filteredApps.map((entry) => {
            const isProtected = protectedApps.has(entry.packageName);
            const strictness = strictnessLevels.get(entry.packageName) || 'normal';

            return (
              <GameRow
                key={entry.packageName}
                entry={entry}
                isProtected={isProtected}
                strictness={strictness}
                locale={locale}
                onToggleProtected={() => onToggleProtected(entry.packageName, !isProtected)}
                onCycleStrictness={(e) => {
                  e.stopPropagation();
                  onCycleStrictness(entry.packageName);
                }}
              />
            );
          })
        )}
      </div>

      {/* Custom App Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
          <div className="w-full max-w-md bg-[#0F141D] border border-white/[0.12] rounded-[24px] p-6 shadow-2xl space-y-5">
            <div className="flex items-center justify-between">
              <h4 className="text-base font-bold text-white flex items-center gap-2">
                <Plus className="w-4 h-4 text-[#3DFFC4]" />
                <span>{t.simulator_custom_app}</span>
              </h4>
              <button
                onClick={() => setShowAddModal(false)}
                className="p-1 rounded-lg text-[#93A1AF] hover:text-white hover:bg-white/[0.06]"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleAddSubmit} className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-white/90">
                  {locale === 'zh' ? '应用或游戏名称' : 'App or Game Name'}
                </label>
                <input
                  type="text"
                  required
                  value={customName}
                  onChange={(e) => setCustomName(e.target.value)}
                  placeholder={locale === 'zh' ? '例如：暗区突围 / Apex 英雄' : 'e.g., Apex Legends Mobile'}
                  className="w-full px-3.5 py-2.5 bg-[#171F2C] border border-white/[0.1] rounded-xl text-xs text-white focus:outline-none focus:border-[#3DFFC4]"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-white/90">
                  {locale === 'zh' ? '包名 (可选)' : 'Package Name (Optional)'}
                </label>
                <input
                  type="text"
                  value={customPackage}
                  onChange={(e) => setCustomPackage(e.target.value)}
                  placeholder={locale === 'zh' ? '例如：com.tencent.tmgp.pubgmhd' : 'e.g., com.ea.gp.apexlegendsmobile'}
                  className="w-full px-3.5 py-2.5 bg-[#171F2C] border border-white/[0.1] rounded-xl text-xs text-white font-mono focus:outline-none focus:border-[#3DFFC4]"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 text-xs font-semibold text-[#93A1AF] hover:text-white"
                >
                  {locale === 'zh' ? '取消' : 'Cancel'}
                </button>
                <button
                  type="submit"
                  className="px-5 py-2.5 text-xs font-bold bg-[#3DFFC4] text-[#03261C] rounded-xl hover:bg-[#5EEAD4] transition-all shadow-md active:scale-95"
                >
                  {locale === 'zh' ? '确认添加' : 'Add Game'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
