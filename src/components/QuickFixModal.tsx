import React, { useState } from 'react';
import {
  BatteryCharging,
  CheckCircle2,
  AlertTriangle,
  ExternalLink,
  Copy,
  Check,
  X,
  Smartphone,
  Terminal,
  Zap,
  RotateCw,
} from 'lucide-react';
import { BatteryOptManager, BatteryOptStatus } from '../automation/BatteryOptManager';
import { OemBrand } from '../types';
import { translations, Locale } from '../i18n/translations';

interface QuickFixModalProps {
  isOpen: boolean;
  selectedBrand: OemBrand;
  locale: Locale;
  onClose: () => void;
  onShowToast: (msg: string) => void;
}

export const QuickFixModal: React.FC<QuickFixModalProps> = ({
  isOpen,
  selectedBrand,
  locale,
  onClose,
  onShowToast,
}) => {
  const t = translations[locale];
  const [copied, setCopied] = useState(false);
  const [status, setStatus] = useState<BatteryOptStatus>(BatteryOptManager.status);

  if (!isOpen) return null;

  const isExempt = status === 'exempt';
  const oemGuide = BatteryOptManager.getOemActionGuide(selectedBrand);
  const adbCmd = BatteryOptManager.getAdbCommand();

  const handleCopyAdb = () => {
    navigator.clipboard.writeText(adbCmd);
    setCopied(true);
    onShowToast(t.quick_fix_copied);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleOpenSystemSettings = () => {
    try {
      // On real Android devices, this intent launches standard Battery Optimization Settings
      window.location.href = BatteryOptManager.getSystemIntentUri();
      onShowToast(
        locale === 'zh'
          ? '正在向系统发起电池优化设置跳转请求…'
          : 'Launching Android Battery Optimization Settings intent…'
      );
    } catch {
      onShowToast(
        locale === 'zh'
          ? '请在系统设置中找到 Aegis 并关闭电池优化'
          : 'Please open your phone settings to exempt Aegis'
      );
    }
  };

  const handleOpenDirectRequest = () => {
    try {
      window.location.href = BatteryOptManager.getDirectRequestUri();
      onShowToast(
        locale === 'zh'
          ? '正在向系统发起白名单弹窗请求…'
          : 'Requesting direct whitelist exemption dialog…'
      );
    } catch {
      onShowToast(
        locale === 'zh'
          ? '请在系统设置中手动开启无限制'
          : 'Please manually set battery usage to Unrestricted'
      );
    }
  };

  const handleToggleSimulatedStatus = () => {
    BatteryOptManager.toggleStatus();
    setStatus(BatteryOptManager.status);
    onShowToast(
      BatteryOptManager.isExempt
        ? locale === 'zh'
          ? '已设置为：不受限制（白名单豁免）'
          : 'Exemption status: Whitelisted (Unrestricted)'
        : locale === 'zh'
        ? '已设置为：受电量优化限制'
        : 'Exemption status: Battery Optimizations Active'
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
      <div className="w-full max-w-lg bg-[#0D121B] border border-white/[0.12] rounded-[28px] p-6 sm:p-7 shadow-2xl space-y-6 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-white/[0.08] pb-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#FBBF24]/15 border border-[#FBBF24]/30 flex items-center justify-center text-[#FBBF24]">
              <BatteryCharging className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white tracking-tight">
                {t.quick_fix_title}
              </h3>
              <p className="text-xs text-[#93A1AF]">
                {locale === 'zh' ? '检测并一键直达系统设置以保障后台存活' : 'Inspect and open system settings to prevent background kills'}
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-[#93A1AF] hover:text-white hover:bg-white/[0.06] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Real-Time Detection Status Card */}
        <div
          className={`p-4 rounded-2xl border flex items-center justify-between gap-4 transition-all ${
            isExempt
              ? 'bg-[#3DFFC4]/10 border-[#3DFFC4]/30 text-[#3DFFC4]'
              : 'bg-[#FBBF24]/10 border-[#FBBF24]/30 text-[#FBBF24]'
          }`}
        >
          <div className="flex items-center gap-3 min-w-0">
            {isExempt ? (
              <CheckCircle2 className="w-5 h-5 shrink-0 text-[#3DFFC4]" />
            ) : (
              <AlertTriangle className="w-5 h-5 shrink-0 text-[#FBBF24]" />
            )}
            <div className="min-w-0">
              <span className="text-xs font-bold block truncate">
                {isExempt ? t.quick_fix_status_exempt : t.quick_fix_status_optimizing}
              </span>
              <span className="text-[11px] text-[#93A1AF] block">
                {isExempt
                  ? (locale === 'zh' ? '系统不会休眠或冻结 Aegis 的后台守护服务' : 'OS will not freeze or kill Aegis background service')
                  : (locale === 'zh' ? '长时间息屏或内存紧张时可能被系统挂起' : 'Service may be suspended when screen is off or RAM is low')}
              </span>
            </div>
          </div>

          <button
            onClick={handleToggleSimulatedStatus}
            title={t.quick_fix_test_toggle}
            className="p-2 rounded-xl bg-white/[0.06] hover:bg-white/[0.12] text-white/90 border border-white/10 shrink-0 text-xs flex items-center gap-1 active:scale-95 transition-all"
          >
            <RotateCw className="w-3.5 h-3.5" />
            <span className="hidden sm:inline text-[11px]">{locale === 'zh' ? '测试切换' : 'Simulate'}</span>
          </button>
        </div>

        {/* Action 1: Direct System Settings Shortcut */}
        <div className="space-y-3">
          <label className="text-xs font-bold text-white flex items-center gap-1.5">
            <ExternalLink className="w-3.5 h-3.5 text-[#3DFFC4]" />
            <span>{locale === 'zh' ? '一键直达系统设置 (Android Intent)' : 'Direct System Shortcut (Android Intent)'}</span>
          </label>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
            <button
              onClick={handleOpenSystemSettings}
              className="p-3.5 rounded-xl bg-[#3DFFC4] hover:bg-[#5EEAD4] text-[#03261C] font-bold text-xs flex items-center justify-center gap-2 shadow-lg shadow-[#3DFFC4]/15 active:scale-95 transition-all"
            >
              <ExternalLink className="w-4 h-4 shrink-0" />
              <span>{t.quick_fix_open_settings}</span>
            </button>

            <button
              onClick={handleOpenDirectRequest}
              className="p-3.5 rounded-xl bg-white/[0.05] hover:bg-white/[0.1] text-white border border-white/[0.1] font-semibold text-xs flex items-center justify-center gap-2 active:scale-95 transition-all"
            >
              <Zap className="w-4 h-4 text-[#FBBF24] shrink-0" />
              <span>{t.quick_fix_request_direct}</span>
            </button>
          </div>
        </div>

        {/* Action 2: OEM Specific Path Guide */}
        <div className="p-4 rounded-2xl bg-[#090D14] border border-white/[0.06] space-y-2 text-xs">
          <div className="flex items-center gap-2 text-white font-semibold">
            <Smartphone className="w-4 h-4 text-[#2DD4BF]" />
            <span>{oemGuide.actionName}</span>
          </div>
          <p className="text-[#93A1AF] font-mono text-[11px] leading-relaxed">
            {oemGuide.path}
          </p>
        </div>

        {/* Action 3: ADB Command for Power Users */}
        <div className="space-y-2">
          <label className="text-xs font-bold text-white flex items-center gap-1.5">
            <Terminal className="w-3.5 h-3.5 text-[#3DFFC4]" />
            <span>{t.quick_fix_adb_hint}</span>
          </label>

          <div className="flex items-center justify-between gap-2 p-3 rounded-xl bg-[#080B10] border border-white/[0.08] font-mono text-xs text-[#3DFFC4]">
            <span className="truncate selection:bg-[#3DFFC4]/30">{adbCmd}</span>
            <button
              onClick={handleCopyAdb}
              className="p-1.5 rounded-lg bg-white/[0.06] hover:bg-white/[0.12] text-white shrink-0 active:scale-95 transition-all flex items-center gap-1"
            >
              {copied ? <Check className="w-3.5 h-3.5 text-[#3DFFC4]" /> : <Copy className="w-3.5 h-3.5" />}
              <span className="text-[11px] font-sans font-semibold">{copied ? (locale === 'zh' ? '已复制' : 'Copied') : (locale === 'zh' ? '复制' : 'Copy')}</span>
            </button>
          </div>
        </div>

        {/* Footer */}
        <div className="pt-2 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2.5 rounded-xl bg-white/[0.08] hover:bg-white/[0.15] text-white text-xs font-semibold active:scale-95 transition-all"
          >
            {locale === 'zh' ? '完成' : 'Done'}
          </button>
        </div>
      </div>
    </div>
  );
};
