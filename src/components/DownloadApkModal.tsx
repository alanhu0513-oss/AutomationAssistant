import React from 'react';
import { Download, ExternalLink, X, Smartphone, ShieldCheck, CheckCircle2, GitBranch } from 'lucide-react';
import { translations, Locale } from '../i18n/translations';

interface DownloadApkModalProps {
  isOpen: boolean;
  locale: Locale;
  onClose: () => void;
  onShowToast: (msg: string) => void;
}

export const DownloadApkModal: React.FC<DownloadApkModalProps> = ({
  isOpen,
  locale,
  onClose,
  onShowToast,
}) => {
  const t = translations[locale];

  if (!isOpen) return null;

  const githubRepoUrl = 'https://github.com/aidenauu04l7/aegis';
  const latestReleaseApkUrl = `${githubRepoUrl}/releases/latest/download/app-debug.apk`;
  const githubActionsUrl = `${githubRepoUrl}/actions`;

  const handleDownloadClick = () => {
    // Trigger download of release APK
    window.open(latestReleaseApkUrl, '_blank');
    onShowToast(
      locale === 'zh'
        ? '正在从 GitHub Releases 启动 APK 下载…'
        : 'Starting APK download from GitHub Releases…'
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
      <div className="w-full max-w-lg bg-[#0D121B] border border-white/[0.12] rounded-[28px] p-6 sm:p-7 shadow-2xl space-y-6 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-white/[0.08] pb-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#3DFFC4]/15 border border-[#3DFFC4]/30 flex items-center justify-center text-[#3DFFC4]">
              <Smartphone className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white tracking-tight">
                {t.download_apk_title}
              </h3>
              <p className="text-xs text-[#93A1AF]">
                {locale === 'zh' ? '在手机上安装原生 APK，实现全自动后台守护' : 'Install native Android APK for full real-time background protection'}
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

        {/* Primary Download Button */}
        <div className="space-y-3">
          <button
            onClick={handleDownloadClick}
            className="w-full py-4 px-6 rounded-2xl bg-[#3DFFC4] hover:bg-[#5EEAD4] text-[#03261C] font-extrabold text-sm flex items-center justify-center gap-2.5 shadow-xl shadow-[#3DFFC4]/20 active:scale-98 transition-all"
          >
            <Download className="w-5 h-5 stroke-[2.5]" />
            <span>{locale === 'zh' ? '立即下载 Aegis.apk (v2.2.0)' : 'Download Aegis.apk (v2.2.0)'}</span>
          </button>

          <p className="text-[11px] text-[#93A1AF] text-center">
            {locale === 'zh'
              ? '编译自 GitHub Actions CI 工作流 · 原生 Android 10+ 架构 · 零广告 零隐私收集'
              : 'Built via GitHub Actions CI workflow · Android 10+ Native · Zero ads & telemetry'}
          </p>
        </div>

        {/* 3 Step Installation Walkthrough */}
        <div className="space-y-3 pt-2">
          <h4 className="text-xs font-bold text-white flex items-center gap-2">
            <ShieldCheck className="w-4 h-4 text-[#3DFFC4]" />
            <span>{locale === 'zh' ? '安装与激活步骤' : 'Installation & Setup Guide'}</span>
          </h4>

          <div className="space-y-2.5">
            <div className="p-3.5 rounded-xl bg-[#090D14] border border-white/[0.06] flex items-start gap-3">
              <span className="w-5 h-5 rounded-lg bg-[#3DFFC4]/15 text-[#3DFFC4] font-bold text-xs flex items-center justify-center shrink-0 mt-0.5 border border-[#3DFFC4]/30">
                1
              </span>
              <div className="text-xs space-y-0.5">
                <span className="font-semibold text-white block">
                  {locale === 'zh' ? '下载并允许未知来源安装' : 'Download & Allow Unknown Sources'}
                </span>
                <span className="text-[#93A1AF] text-[11px] leading-relaxed block">
                  {locale === 'zh'
                    ? '下载完成后点击通知栏中的 APK 文件，根据系统提示允许浏览器安装应用。'
                    : 'Tap the downloaded APK file in Downloads or notifications, and grant permission to install.'}
                </span>
              </div>
            </div>

            <div className="p-3.5 rounded-xl bg-[#090D14] border border-white/[0.06] flex items-start gap-3">
              <span className="w-5 h-5 rounded-lg bg-[#3DFFC4]/15 text-[#3DFFC4] font-bold text-xs flex items-center justify-center shrink-0 mt-0.5 border border-[#3DFFC4]/30">
                2
              </span>
              <div className="text-xs space-y-0.5">
                <span className="font-semibold text-white block">
                  {locale === 'zh' ? '开启「无障碍服务」' : 'Enable Accessibility Service'}
                </span>
                <span className="text-[#93A1AF] text-[11px] leading-relaxed block">
                  {locale === 'zh'
                    ? '打开应用后点击启动，在系统「已下载的应用 / 无障碍」中找到 Aegis 并开启。'
                    : 'Launch Aegis on phone, tap Activate, and turn on the service in Accessibility Settings.'}
                </span>
              </div>
            </div>

            <div className="p-3.5 rounded-xl bg-[#090D14] border border-white/[0.06] flex items-start gap-3">
              <span className="w-5 h-5 rounded-lg bg-[#3DFFC4]/15 text-[#3DFFC4] font-bold text-xs flex items-center justify-center shrink-0 mt-0.5 border border-[#3DFFC4]/30">
                3
              </span>
              <div className="text-xs space-y-0.5">
                <span className="font-semibold text-white block">
                  {locale === 'zh' ? '运行「一键修复」免除电池查杀' : 'Apply Quick Fix Battery Exemption'}
                </span>
                <span className="text-[#93A1AF] text-[11px] leading-relaxed block">
                  {locale === 'zh'
                    ? '在应用内使用 Quick Fix 功能，将 Aegis 设置为不受电量优化限制，防止后台挂起。'
                    : 'Use the Quick Fix button to whitelist Aegis from OS battery savers for uninterrupted gaming.'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* GitHub Repository Links */}
        <div className="pt-2 flex flex-col sm:flex-row items-center justify-between gap-3 border-t border-white/[0.06]">
          <a
            href={githubActionsUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="text-xs font-semibold text-[#93A1AF] hover:text-white flex items-center gap-1.5 transition-colors"
          >
            <GitBranch className="w-3.5 h-3.5 text-[#3DFFC4]" />
            <span>GitHub Actions CI Builds</span>
            <ExternalLink className="w-3 h-3" />
          </a>

          <button
            onClick={onClose}
            className="px-5 py-2 rounded-xl bg-white/[0.08] hover:bg-white/[0.15] text-white text-xs font-semibold active:scale-95 transition-all w-full sm:w-auto"
          >
            {locale === 'zh' ? '关闭' : 'Close'}
          </button>
        </div>
      </div>
    </div>
  );
};
