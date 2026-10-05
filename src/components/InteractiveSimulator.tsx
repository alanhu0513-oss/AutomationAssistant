import React, { useState, useEffect } from 'react';
import {
  Zap,
  Play,
  PhoneCall,
  Home,
  ShieldAlert,
  AlertCircle,
  CheckCircle2,
  XCircle,
  Smartphone,
  Eye,
  Sparkles,
} from 'lucide-react';
import { GlassCard } from './GlassCard';
import { OverlayEngine } from '../automation/OverlayEngine';
import { TargetStore } from '../automation/TargetStore';
import { AutomationState } from '../automation/AutomationState';
import { ShieldLog } from '../automation/ShieldLog';
import { AccessibilityWindowType, EngineAction, WindowEvent } from '../types';
import { translations, Locale } from '../i18n/translations';

interface InteractiveSimulatorProps {
  locale: Locale;
  onShowToast: (msg: string) => void;
}

export const InteractiveSimulator: React.FC<InteractiveSimulatorProps> = ({
  locale,
  onShowToast,
}) => {
  const t = translations[locale];

  const [engine, setEngine] = useState<OverlayEngine | null>(null);
  const [selectedForeground, setSelectedForeground] = useState('com.tencent.tmgp.pubgmhd');
  const [lastActionMessage, setLastActionMessage] = useState<string | null>(null);
  const [activeScreenPopup, setActiveScreenPopup] = useState<{
    label: string;
    pkg: string;
    status: 'active' | 'dismissed' | 'exempt' | 'ignored';
  } | null>(null);

  const [simState, setSimState] = useState<{
    isRunning: boolean;
    previewMode: boolean;
    protectedApps: Set<string>;
  }>({
    isRunning: AutomationState.isRunning,
    previewMode: TargetStore.previewMode,
    protectedApps: TargetStore.protectedApps,
  });

  useEffect(() => {
    const homePackages = new Set(['com.android.launcher3', 'com.mi.android.globallauncher', 'com.sec.android.app.launcher']);
    const exemptPackages = new Set(['com.google.android.dialer', 'com.samsung.android.dialer', 'com.android.incallui']);
    const systemPackages = new Set([
      'com.miui.powerkeeper',
      'com.vivo.permissionmanager',
      'com.coloros.safecenter',
      'com.android.packageinstaller',
      'com.google.android.permissioncontroller',
    ]);

    const eng = new OverlayEngine(
      'dev.aegis.shield',
      homePackages,
      exemptPackages,
      (pkg) => systemPackages.has(pkg),
      (foreground) => {
        const level = TargetStore.strictnessFor(foreground);
        switch (level) {
          case 'gentle':
            return 1000;
          case 'strict':
            return 100;
          case 'normal':
          default:
            return 400;
        }
      }
    );

    eng.onEvent(
      {
        type: 32,
        packageName: selectedForeground,
        className: 'MainActivity',
        windowType: AccessibilityWindowType.TYPE_APPLICATION,
        at: Date.now(),
      },
      TargetStore.protectedApps
    );

    setEngine(eng);

    const unsubTarget = TargetStore.subscribe(() => {
      setSimState({
        isRunning: AutomationState.isRunning,
        previewMode: TargetStore.previewMode,
        protectedApps: TargetStore.protectedApps,
      });
    });

    const unsubAuto = AutomationState.subscribe(() => {
      setSimState({
        isRunning: AutomationState.isRunning,
        previewMode: TargetStore.previewMode,
        protectedApps: TargetStore.protectedApps,
      });
    });

    return () => {
      unsubTarget();
      unsubAuto();
    };
  }, []);

  const handleSimulateEvent = (
    eventPkg: string,
    eventLabel: string,
    windowType: AccessibilityWindowType,
    className?: string
  ) => {
    if (!engine) return;

    if (!AutomationState.isRunning) {
      const msg = t.sim_engine_off;
      setLastActionMessage(msg);
      onShowToast(msg);
      return;
    }

    const now = Date.now();
    const event: WindowEvent = {
      type: 32,
      packageName: eventPkg,
      className: className || 'DialogActivity',
      windowType: windowType,
      at: now,
    };

    const action = engine.onEvent(event, TargetStore.protectedApps);
    const isProtected = TargetStore.protectedApps.has(selectedForeground);
    const isPreview = TargetStore.previewMode;

    if (eventPkg === 'com.google.android.dialer') {
      setActiveScreenPopup({ label: eventLabel, pkg: eventPkg, status: 'exempt' });
      setLastActionMessage(t.sim_call_exempt);
      onShowToast(t.sim_call_exempt);
      setTimeout(() => setActiveScreenPopup(null), 2500);
      return;
    }

    if (eventPkg === 'com.android.launcher3') {
      setActiveScreenPopup({ label: eventLabel, pkg: eventPkg, status: 'ignored' });
      setLastActionMessage(t.sim_home_adopted);
      onShowToast(t.sim_home_adopted);
      setTimeout(() => setActiveScreenPopup(null), 2500);
      return;
    }

    if (!isProtected) {
      setActiveScreenPopup({ label: eventLabel, pkg: eventPkg, status: 'ignored' });
      const msg = t.sim_unprotected(selectedForeground);
      setLastActionMessage(msg);
      onShowToast(msg);
      setTimeout(() => setActiveScreenPopup(null), 3000);
      return;
    }

    // Set active popup on visual phone frame
    setActiveScreenPopup({ label: eventLabel, pkg: eventPkg, status: 'active' });

    if (action === EngineAction.FIRE) {
      engine.onFired(now);
      if (isPreview) {
        ShieldLog.record({
          atEpochMillis: now,
          overlayPackage: eventPkg,
          overlayLabel: eventLabel,
          gameLabel: selectedForeground,
          preview: true,
        });
        const msg = t.sim_popup_preview(eventLabel);
        setLastActionMessage(msg);
        onShowToast(msg);
        setTimeout(() => {
          setActiveScreenPopup({ label: eventLabel, pkg: eventPkg, status: 'dismissed' });
          setTimeout(() => setActiveScreenPopup(null), 1000);
        }, 500);
      } else {
        AutomationState.recordBlocked();
        ShieldLog.record({
          atEpochMillis: now,
          overlayPackage: eventPkg,
          overlayLabel: eventLabel,
          gameLabel: selectedForeground,
          preview: false,
        });
        const msg = t.sim_popup_dismissed(eventLabel);
        setLastActionMessage(msg);
        onShowToast(msg);
        setTimeout(() => {
          setActiveScreenPopup({ label: eventLabel, pkg: eventPkg, status: 'dismissed' });
          setTimeout(() => setActiveScreenPopup(null), 800);
        }, 300);
      }
    }
  };

  const handleForegroundChange = (pkg: string) => {
    setSelectedForeground(pkg);
    if (engine) {
      engine.onEvent(
        {
          type: 32,
          packageName: pkg,
          className: 'MainActivity',
          windowType: AccessibilityWindowType.TYPE_APPLICATION,
          at: Date.now(),
        },
        TargetStore.protectedApps
      );
    }
  };

  const getForegroundGameName = () => {
    switch (selectedForeground) {
      case 'com.tencent.tmgp.pubgmhd':
        return 'PUBG Mobile';
      case 'com.miHoYo.GenshinImpact':
        return 'Genshin Impact';
      case 'com.proximabeta.mf.uamo':
        return 'Delta Force';
      case 'com.tencent.tmgp.sgame':
        return 'Honor of Kings';
      default:
        return 'Call of Duty: Mobile';
    }
  };

  return (
    <GlassCard borderAccent="teal" className="p-6 sm:p-7 space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-white/[0.08] pb-4">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Zap className="w-4 h-4 text-[#2DD4BF]" />
            <span>{t.simulator_title}</span>
          </h3>
          <p className="text-xs text-[#93A1AF]">{t.simulator_desc}</p>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-xs text-[#93A1AF]">
            {locale === 'zh' ? '当前模式' : 'Mode'}:
          </span>
          <span className="text-xs font-mono font-semibold text-[#2DD4BF]">
            {simState.previewMode ? (locale === 'zh' ? '仅记录 (预览)' : 'Preview (Log Only)') : (locale === 'zh' ? '即时拦截' : 'Instant Dismiss')}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left Side: Controls & Triggers (7 cols) */}
        <div className="lg:col-span-7 space-y-4">
          {/* Active Foreground Selection */}
          <div className="space-y-2 p-4 rounded-2xl bg-[#0B0F17] border border-white/[0.08]">
            <div className="flex items-center justify-between">
              <label className="text-xs font-semibold text-white flex items-center gap-1.5">
                <Play className="w-3.5 h-3.5 text-[#3DFFC4]" />
                <span>{t.simulator_foreground}</span>
              </label>
              <span className={`text-[11px] font-semibold ${simState.protectedApps.has(selectedForeground) ? 'text-[#3DFFC4]' : 'text-[#FF5C5C]'}`}>
                {simState.protectedApps.has(selectedForeground)
                  ? (locale === 'zh' ? '● 护盾生效中' : '● Armed')
                  : (locale === 'zh' ? '○ 未勾选保护' : '○ Not Protected')}
              </span>
            </div>

            <select
              value={selectedForeground}
              onChange={(e) => handleForegroundChange(e.target.value)}
              className="w-full bg-[#141B24] text-xs font-medium text-white border border-white/10 rounded-xl px-3 py-2 focus:outline-none focus:border-[#3DFFC4] cursor-pointer"
            >
              <option value="com.tencent.tmgp.pubgmhd">PUBG Mobile (com.tencent.tmgp.pubgmhd)</option>
              <option value="com.miHoYo.GenshinImpact">Genshin Impact (com.miHoYo.GenshinImpact)</option>
              <option value="com.proximabeta.mf.uamo">Delta Force (com.proximabeta.mf.uamo)</option>
              <option value="com.tencent.tmgp.sgame">Honor of Kings (com.tencent.tmgp.sgame)</option>
              <option value="com.activision.callofduty.shooter">Call of Duty: Mobile (com.activision.callofduty.shooter)</option>
            </select>
          </div>

          {/* Trigger Simulation Event Buttons */}
          <div className="space-y-2.5">
            <label className="text-xs font-semibold text-[#93A1AF] block">
              {t.simulator_popup_trigger}
            </label>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
              <button
                onClick={() =>
                  handleSimulateEvent(
                    'com.miui.powerkeeper',
                    'MIUI Game Time Alert',
                    AccessibilityWindowType.TYPE_SYSTEM
                  )
                }
                className="p-3 rounded-xl bg-white/[0.03] hover:bg-[#3DFFC4]/15 border border-white/[0.08] hover:border-[#3DFFC4]/40 text-left transition-all duration-200 group active:scale-95"
              >
                <div className="flex items-center gap-2 font-semibold text-xs text-white group-hover:text-[#3DFFC4]">
                  <ShieldAlert className="w-4 h-4 text-[#3DFFC4] shrink-0" />
                  <span>Xiaomi/Vivo Timer</span>
                </div>
                <p className="text-[10px] text-[#93A1AF] mt-1 font-mono">
                  TYPE_SYSTEM · Auto-Back
                </p>
              </button>

              <button
                onClick={() =>
                  handleSimulateEvent(
                    'com.android.packageinstaller',
                    'Permission Dialog',
                    AccessibilityWindowType.TYPE_ACCESSIBILITY_OVERLAY
                  )
                }
                className="p-3 rounded-xl bg-white/[0.03] hover:bg-[#FBBF24]/15 border border-white/[0.08] hover:border-[#FBBF24]/40 text-left transition-all duration-200 group active:scale-95"
              >
                <div className="flex items-center gap-2 font-semibold text-xs text-white group-hover:text-[#FBBF24]">
                  <AlertCircle className="w-4 h-4 text-[#FBBF24] shrink-0" />
                  <span>Permission Dialog</span>
                </div>
                <p className="text-[10px] text-[#93A1AF] mt-1 font-mono">
                  TYPE_ACCESSIBILITY · Auto-Back
                </p>
              </button>

              <button
                onClick={() =>
                  handleSimulateEvent(
                    'com.google.android.dialer',
                    'Incoming Call Screen',
                    AccessibilityWindowType.TYPE_APPLICATION
                  )
                }
                className="p-3 rounded-xl bg-white/[0.03] hover:bg-white/[0.08] border border-white/[0.08] text-left transition-all duration-200 group active:scale-95"
              >
                <div className="flex items-center gap-2 font-semibold text-xs text-white/90">
                  <PhoneCall className="w-4 h-4 text-blue-400 shrink-0" />
                  <span>Incoming Call</span>
                </div>
                <p className="text-[10px] text-[#93A1AF] mt-1 font-mono">
                  Exempt · Will Not Dismiss
                </p>
              </button>

              <button
                onClick={() =>
                  handleSimulateEvent(
                    'com.android.launcher3',
                    'Home Launcher',
                    AccessibilityWindowType.TYPE_APPLICATION
                  )
                }
                className="p-3 rounded-xl bg-white/[0.03] hover:bg-white/[0.08] border border-white/[0.08] text-left transition-all duration-200 group active:scale-95"
              >
                <div className="flex items-center gap-2 font-semibold text-xs text-white/90">
                  <Home className="w-4 h-4 text-purple-400 shrink-0" />
                  <span>Home Launcher</span>
                </div>
                <p className="text-[10px] text-[#93A1AF] mt-1 font-mono">
                  Adopts Foreground State
                </p>
              </button>
            </div>
          </div>

          {/* Real-Time Telemetry Log Line */}
          {lastActionMessage && (
            <div className="p-3.5 rounded-xl bg-[#090D14] border border-[#3DFFC4]/30 text-xs text-[#3DFFC4] flex items-center gap-2.5 animate-fade-in font-mono">
              <CheckCircle2 className="w-4 h-4 shrink-0 text-[#3DFFC4]" />
              <span className="truncate">{lastActionMessage}</span>
            </div>
          )}
        </div>

        {/* Right Side: Animated Phone Simulation Mockup (5 cols) */}
        <div className="lg:col-span-5 flex flex-col items-center justify-center">
          <div className="w-full max-w-[260px] aspect-[9/17] bg-[#05070B] border-2 border-slate-700/80 rounded-[36px] p-2.5 shadow-2xl relative flex flex-col justify-between overflow-hidden">
            {/* Phone Speaker Notch */}
            <div className="w-20 h-3 bg-slate-800 rounded-full mx-auto mb-2 shrink-0 z-20" />

            {/* Game Screen Canvas */}
            <div className="flex-1 w-full bg-gradient-to-b from-slate-900 via-[#0E1520] to-[#0A0E17] rounded-[24px] relative flex flex-col items-center justify-center p-3 text-center overflow-hidden border border-white/[0.05]">
              {/* Game Background Graphics */}
              <div className="space-y-1.5 z-10">
                <div className="w-12 h-12 rounded-2xl bg-[#3DFFC4]/15 border border-[#3DFFC4]/30 mx-auto flex items-center justify-center text-[#3DFFC4]">
                  <Sparkles className="w-6 h-6" />
                </div>
                <h5 className="text-xs font-bold text-white truncate max-w-[180px]">
                  {getForegroundGameName()}
                </h5>
                <span className="text-[10px] text-[#3DFFC4] font-mono block">
                  FPS: 60 · In-Game
                </span>
              </div>

              {/* Dynamic Popup Simulation Overlay */}
              {activeScreenPopup && (
                <div
                  className={`absolute inset-3 z-30 rounded-2xl flex flex-col items-center justify-center p-3 text-center backdrop-blur-md transition-all duration-300 ${
                    activeScreenPopup.status === 'dismissed'
                      ? 'scale-90 opacity-0 bg-[#3DFFC4]/20 border border-[#3DFFC4]'
                      : activeScreenPopup.status === 'exempt'
                      ? 'bg-blue-950/90 border border-blue-400 scale-100 opacity-100'
                      : activeScreenPopup.status === 'ignored'
                      ? 'bg-red-950/90 border border-red-500 scale-100 opacity-100'
                      : 'bg-[#141B24]/95 border border-[#FBBF24]/80 scale-100 opacity-100 animate-fade-in shadow-2xl'
                  }`}
                >
                  {activeScreenPopup.status === 'dismissed' ? (
                    <div className="space-y-1 text-[#3DFFC4]">
                      <CheckCircle2 className="w-7 h-7 mx-auto" />
                      <p className="text-[11px] font-bold">DISMISSED</p>
                    </div>
                  ) : activeScreenPopup.status === 'exempt' ? (
                    <div className="space-y-1 text-blue-300">
                      <PhoneCall className="w-7 h-7 mx-auto" />
                      <p className="text-[11px] font-bold">Incoming Call (Exempt)</p>
                    </div>
                  ) : activeScreenPopup.status === 'ignored' ? (
                    <div className="space-y-1 text-red-300">
                      <XCircle className="w-7 h-7 mx-auto" />
                      <p className="text-[10px] font-bold">Unprotected / Ignored</p>
                    </div>
                  ) : (
                    <div className="space-y-1.5 text-white">
                      <AlertCircle className="w-6 h-6 text-[#FBBF24] mx-auto animate-bounce" />
                      <p className="text-[11px] font-bold leading-tight">{activeScreenPopup.label}</p>
                      <span className="text-[9px] text-[#3DFFC4] font-mono block animate-pulse">
                        Auto-Intercepting...
                      </span>
                    </div>
                  )}
                </div>
              )}
            </div>

            {/* Bottom Home Indicator */}
            <div className="w-24 h-1 bg-slate-700/60 rounded-full mx-auto mt-2 shrink-0 z-20" />
          </div>
        </div>
      </div>
    </GlassCard>
  );
};
