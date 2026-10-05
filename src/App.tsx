import React, { useState, useEffect } from 'react';
import { Header, NavTab } from './components/Header';
import { StatusCard } from './components/StatusCard';
import { ShieldToggle } from './components/ShieldToggle';
import { OfflineBanner } from './components/OfflineBanner';
import { BatteryStatusBanner } from './components/BatteryStatusBanner';
import { DeviceHealthCard } from './components/DeviceHealthCard';
import { PreviewToggleCard } from './components/PreviewToggleCard';
import { GamesSection } from './components/GamesSection';
import { SystemCapabilitiesCard } from './components/SystemCapabilitiesCard';
import { ShieldLogCard } from './components/ShieldLogCard';
import { UpdateCard } from './components/UpdateCard';
import { NotificationPermissionDialog } from './components/NotificationPermissionDialog';
import { Onboarding } from './components/Onboarding';
import { InteractiveSimulator } from './components/InteractiveSimulator';
import { QuickFixModal } from './components/QuickFixModal';
import { DownloadApkModal } from './components/DownloadApkModal';
import { Toast } from './components/Toast';

import { TargetStore } from './automation/TargetStore';
import { AutomationState } from './automation/AutomationState';
import { ShieldLog } from './automation/ShieldLog';
import { BatteryOptManager, BatteryOptStatus } from './automation/BatteryOptManager';
import { AppRepository } from './data/AppRepository';
import { UpdateChecker } from './data/UpdateChecker';
import { AppEntry, LogEntry, OemBrand, StrictnessLevel, UpdateInfo } from './types';
import { Locale } from './i18n/translations';

const appRepo = new AppRepository('dev.aegis.shield');
const updateChecker = new UpdateChecker();

export const App: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<NavTab>('shield');
  const [isFirstLaunch, setIsFirstLaunch] = useState(TargetStore.isFirstLaunch);
  const [isRunning, setIsRunning] = useState(AutomationState.isRunning);
  const [blockedCount, setBlockedCount] = useState(AutomationState.blockedCount);
  const [serviceEverEnabled, setServiceEverEnabled] = useState(TargetStore.serviceEverEnabled);
  const [protectedApps, setProtectedApps] = useState<Set<string>>(TargetStore.protectedApps);
  const [strictnessLevels, setStrictnessLevels] = useState<Map<string, StrictnessLevel>>(TargetStore.strictnessLevels);
  const [previewMode, setPreviewMode] = useState(TargetStore.previewMode);
  const [selectedBrand, setSelectedBrand] = useState<OemBrand>(TargetStore.selectedBrand);
  const [locale, setLocale] = useState<Locale>(TargetStore.locale);
  const [batteryOptStatus, setBatteryOptStatus] = useState<BatteryOptStatus>(BatteryOptManager.status);

  const [shieldLogEntries, setShieldLogEntries] = useState<LogEntry[]>(ShieldLog.getEntries());
  const [apps, setApps] = useState<AppEntry[]>([]);
  const [loadingApps, setLoadingApps] = useState(true);
  const [updateInfo, setUpdateInfo] = useState<UpdateInfo | null>(null);

  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [showNotifDialog, setShowNotifDialog] = useState(false);
  const [showQuickFixModal, setShowQuickFixModal] = useState(false);
  const [showDownloadApkModal, setShowDownloadApkModal] = useState(false);

  // Sync state subscriptions
  useEffect(() => {
    const unsubTarget = TargetStore.subscribe(() => {
      setIsFirstLaunch(TargetStore.isFirstLaunch);
      setServiceEverEnabled(TargetStore.serviceEverEnabled);
      setProtectedApps(TargetStore.protectedApps);
      setStrictnessLevels(TargetStore.strictnessLevels);
      setPreviewMode(TargetStore.previewMode);
      setSelectedBrand(TargetStore.selectedBrand);
      setLocale(TargetStore.locale);
    });

    const unsubAuto = AutomationState.subscribe(() => {
      setIsRunning(AutomationState.isRunning);
      setBlockedCount(AutomationState.blockedCount);
      if (AutomationState.lastError) {
        showToast(AutomationState.lastError);
        AutomationState.clearError();
      }
    });

    const unsubLog = ShieldLog.subscribe((entries) => {
      setShieldLogEntries(entries);
    });

    const unsubBattery = BatteryOptManager.subscribe(() => {
      setBatteryOptStatus(BatteryOptManager.status);
    });

    if (!TargetStore.notificationsPrompted) {
      setShowNotifDialog(true);
    }

    loadApps();
    checkUpdates();

    return () => {
      unsubTarget();
      unsubAuto();
      unsubLog();
      unsubBattery();
    };
  }, []);

  const loadApps = async () => {
    setLoadingApps(true);
    try {
      const list = await appRepo.loadUserApps();
      setApps(list);
    } catch {
      showToast('Could not load apps list');
    } finally {
      setLoadingApps(false);
    }
  };

  const checkUpdates = async () => {
    try {
      const result = await updateChecker.check('2.2.0');
      if (result.status === 'available') {
        setUpdateInfo(result.info);
      }
    } catch {
      // Silent fail
    }
  };

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage((current) => (current === msg ? null : current));
    }, 3500);
  };

  const handleToggleRunning = () => {
    const nextRunning = !isRunning;
    AutomationState.setRunning(nextRunning);
    if (nextRunning) {
      TargetStore.markServiceEverEnabled();
      showToast(locale === 'zh' ? '护盾引擎已开启' : 'Shield Engine active');
    } else {
      showToast(locale === 'zh' ? '护盾引擎已进入待机' : 'Shield Engine standby');
    }
  };

  const handleToggleProtected = (pkg: string, isProt: boolean) => {
    TargetStore.setAppProtected(pkg, isProt);
    const game = apps.find((a) => a.packageName === pkg)?.label || pkg;
    showToast(
      isProt
        ? locale === 'zh'
          ? `已为 ${game} 开启护盾保护`
          : `Armed protection for ${game}`
        : locale === 'zh'
        ? `已取消 ${game} 的保护`
        : `Removed protection for ${game}`
    );
  };

  const handleCycleStrictness = (pkg: string) => {
    const current = TargetStore.strictnessFor(pkg);
    const next: StrictnessLevel =
      current === 'normal' ? 'gentle' : current === 'gentle' ? 'strict' : 'normal';
    TargetStore.setStrictness(pkg, next);
    showToast(
      locale === 'zh'
        ? `反应级别已调整为：${next.toUpperCase()}`
        : `Reaction level: ${next.toUpperCase()}`
    );
  };

  const handleAddCustomApp = (name: string, pkg: string) => {
    appRepo.addCustomApp(name, pkg);
    loadApps();
    showToast(locale === 'zh' ? `已添加游戏：${name}` : `Added game: ${name}`);
  };

  // If first launch is active, show onboarding walkthrough
  if (isFirstLaunch) {
    return (
      <Onboarding
        locale={locale}
        onOpenAccessibilitySettings={() => {
          AutomationState.setRunning(true);
          TargetStore.markServiceEverEnabled();
          showToast(locale === 'zh' ? '已开启守护服务' : 'Granted Service Permission');
        }}
        onFinished={() => {
          TargetStore.completeFirstLaunch();
          showToast(locale === 'zh' ? '欢迎使用 Aegis！' : 'Welcome to Aegis!');
        }}
      />
    );
  }

  return (
    <div className="min-h-screen bg-[#07090E] text-[#E2E8F0] flex flex-col justify-between selection:bg-[#3DFFC4]/25">
      <div>
        {/* Top 3-Zone Sticky App Bar */}
        <Header
          currentTab={currentTab}
          locale={locale}
          isRunning={isRunning}
          onSelectTab={setCurrentTab}
          onToggleLocale={() => TargetStore.setLocale(locale === 'en' ? 'zh' : 'en')}
          onReplayOnboarding={() => TargetStore.resetFirstLaunch()}
          onOpenDownloadApk={() => setShowDownloadApkModal(true)}
        />

        {/* Main Content Area with Smooth Cross-Fade Views */}
        <main className="max-w-4xl mx-auto px-4 sm:px-6 py-6 sm:py-8">
          {/* TAB 1: SHIELD HUB */}
          {currentTab === 'shield' && (
            <div className="space-y-6 animate-fade-in">
              {serviceEverEnabled && !isRunning && (
                <OfflineBanner
                  locale={locale}
                  onReenable={() => {
                    AutomationState.setRunning(true);
                    showToast(locale === 'zh' ? '护盾引擎已重新连接' : 'Shield Engine re-enabled');
                  }}
                />
              )}

              {/* Centerpiece Hero Status */}
              <StatusCard
                isRunning={isRunning}
                selectedGameCount={protectedApps.size}
                blockedCount={blockedCount}
                locale={locale}
                onKeepRunning={() => setShowQuickFixModal(true)}
                onToggleShield={handleToggleRunning}
              />

              {/* Battery Status Banner / Quick Fix Inspector */}
              <BatteryStatusBanner
                status={batteryOptStatus}
                locale={locale}
                onOpenQuickFix={() => setShowQuickFixModal(true)}
              />

              {/* Quick Tactile Switch */}
              <ShieldToggle
                running={isRunning}
                locale={locale}
                onToggle={handleToggleRunning}
              />

              {/* Preview Mode Toggle */}
              <PreviewToggleCard
                enabled={previewMode}
                locale={locale}
                onToggle={(enabled) => {
                  TargetStore.setPreviewMode(enabled);
                  showToast(
                    enabled
                      ? locale === 'zh'
                        ? '预览模式已开启（仅记录不执行关闭）'
                        : 'Preview mode enabled (logging only)'
                      : locale === 'zh'
                      ? '预览模式已关闭（恢复即时关闭）'
                      : 'Preview mode disabled (instant dismissal active)'
                  );
                }}
              />

              {/* Recent Activity Timeline */}
              <ShieldLogCard
                entries={shieldLogEntries}
                locale={locale}
                onClear={() => {
                  ShieldLog.clear();
                  showToast(locale === 'zh' ? '动态日志已清空' : 'Shield activity log cleared');
                }}
              />

              {/* Update Card if available */}
              {updateInfo && (
                <UpdateCard
                  info={updateInfo}
                  locale={locale}
                  onOpenRelease={(url) => window.open(url, '_blank')}
                />
              )}
            </div>
          )}

          {/* TAB 2: GAMES ROSTER */}
          {currentTab === 'games' && (
            <div className="space-y-6 animate-fade-in">
              <GamesSection
                apps={apps}
                loading={loadingApps}
                protectedApps={protectedApps}
                strictnessLevels={strictnessLevels}
                locale={locale}
                onToggleProtected={handleToggleProtected}
                onCycleStrictness={handleCycleStrictness}
                onAddCustomApp={handleAddCustomApp}
              />
            </div>
          )}

          {/* TAB 3: LAB & SIMULATOR */}
          {currentTab === 'simulator' && (
            <div className="space-y-6 animate-fade-in">
              <InteractiveSimulator
                locale={locale}
                onShowToast={showToast}
              />

              <ShieldLogCard
                entries={shieldLogEntries}
                locale={locale}
                onClear={() => {
                  ShieldLog.clear();
                  showToast(locale === 'zh' ? '动态日志已清空' : 'Shield activity log cleared');
                }}
              />
            </div>
          )}

          {/* TAB 4: DEVICE & SYSTEM GUIDE */}
          {currentTab === 'guide' && (
            <div className="space-y-6 animate-fade-in">
              <DeviceHealthCard
                selectedBrand={selectedBrand}
                locale={locale}
                onSelectBrand={(b) => TargetStore.setSelectedBrand(b)}
                onOpenBatterySettings={() => setShowQuickFixModal(true)}
                onOpenQuickFix={() => setShowQuickFixModal(true)}
              />

              <SystemCapabilitiesCard locale={locale} />
            </div>
          )}
        </main>
      </div>

      {/* Quiet Footer with Download Shortcut */}
      <footer className="py-6 border-t border-white/[0.04] text-xs text-[#93A1AF]">
        <div className="max-w-4xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <span className="font-semibold text-white">Aegis · Game Window Protection</span>
            <span aria-hidden="true" className="text-white/20">·</span>
            <span className="font-mono text-[11px] text-[#93A1AF]/80">
              Zero Root · Local First · 100% Privacy
            </span>
          </div>

          <button
            onClick={() => setShowDownloadApkModal(true)}
            className="text-[#3DFFC4] hover:underline font-semibold flex items-center gap-1 text-xs"
          >
            <span>{locale === 'zh' ? '下载 Android 原生 .APK' : 'Download Android .APK'}</span>
          </button>
        </div>
      </footer>

      {/* Notification Permission Dialog */}
      {showNotifDialog && (
        <NotificationPermissionDialog
          locale={locale}
          onAllow={() => {
            setShowNotifDialog(false);
            TargetStore.markNotificationsPrompted();
            showToast(locale === 'zh' ? '通知权限已授予' : 'Notification permission granted');
          }}
          onDismiss={() => {
            setShowNotifDialog(false);
            TargetStore.markNotificationsPrompted();
          }}
        />
      )}

      {/* Quick Fix Battery Optimization Modal */}
      <QuickFixModal
        isOpen={showQuickFixModal}
        selectedBrand={selectedBrand}
        locale={locale}
        onClose={() => setShowQuickFixModal(false)}
        onShowToast={showToast}
      />

      {/* Download APK Modal */}
      <DownloadApkModal
        isOpen={showDownloadApkModal}
        locale={locale}
        onClose={() => setShowDownloadApkModal(false)}
        onShowToast={showToast}
      />

      {/* Toast Feedback */}
      <Toast message={toastMessage} onClose={() => setToastMessage(null)} />
    </div>
  );
};
