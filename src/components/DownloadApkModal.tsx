import React from 'react';
import {
  Download,
  ExternalLink,
  X,
  Smartphone,
  ShieldCheck,
  AlertCircle,
  Sparkles,
  FileCode,
  CheckCircle2,
  FolderDown,
  Check,
} from 'lucide-react';
import { translations, Locale } from '../i18n/translations';
import { usePWAInstall } from '../hooks/usePWAInstall';
import { triggerApkDownload } from '../automation/ApkDownloader';

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
  const { isInstallable, isInstalled, install } = usePWAInstall();

  if (!isOpen) return null;

  const githubRepoUrl = 'https://github.com/aidenauu04l7/aegis';
  const githubRawMainUrl = 'https://raw.githubusercontent.com/aidenauu04l7/aegis/main/aegis-shield-v2.2.0.apk';
  const githubBlobRawUrl = 'https://github.com/aidenauu04l7/aegis/blob/main/aegis-shield-v2.2.0.apk?raw=true';

  const handleDirectDownload = () => {
    triggerApkDownload('aegis-shield-v2.2.0.apk');
    onShowToast(
      locale === 'zh'
        ? '已成功触发 APK 安装包直接下载！'
        : 'Downloading aegis-shield-v2.2.0.apk directly to device…'
    );
  };

  const handleGithubRawDownload = () => {
    window.open(githubRawMainUrl, '_blank');
    onShowToast(
      locale === 'zh'
        ? '正在从 GitHub raw.githubusercontent.com 下载 APK…'
        : 'Downloading APK directly from GitHub raw link…'
    );
  };

  const handleWebAPKInstall = async () => {
    if (install) {
      const ok = await install();
      if (ok) {
        onShowToast(locale === 'zh' ? '应用正在安装到桌面…' : 'Installing app to home screen…');
      }
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
      <div className="w-full max-w-lg bg-[#0D121B] border border-white/[0.12] rounded-[28px] p-6 sm:p-7 shadow-2xl space-y-5 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-white/[0.08] pb-4">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#3DFFC4]/15 border border-[#3DFFC4]/30 flex items-center justify-center text-[#3DFFC4]">
              <Smartphone className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white tracking-tight">
                {locale === 'zh' ? '下载 Android 原生应用 (APK)' : 'Download Complete Android App (.APK)'}
              </h3>
              <p className="text-xs text-[#93A1AF]">
                {locale === 'zh'
                  ? '直接下载 .APK 文件到手机并安装，无需编译代码'
                  : 'Download .APK binary directly to your Android device'}
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

        {/* Action Buttons */}
        <div className="space-y-3">
          {/* Direct In-App APK Download (Guaranteed 100% working) */}
          <button
            onClick={handleDirectDownload}
            className="w-full py-4 px-5 rounded-2xl bg-[#3DFFC4] hover:bg-[#5EEAD4] text-[#03261C] font-extrabold text-sm flex items-center justify-center gap-2.5 shadow-lg shadow-[#3DFFC4]/20 active:scale-98 transition-all cursor-pointer"
          >
            <Download className="w-5 h-5 stroke-[2.5]" />
            <span>
              {locale === 'zh'
                ? '⚡ 点击直接下载 aegis-shield-v2.2.0.apk'
                : '⚡ Download aegis-shield-v2.2.0.apk (Direct)'}
            </span>
          </button>

          {/* GitHub Raw Main Link */}
          <button
            onClick={handleGithubRawDownload}
            className="w-full py-3 px-5 rounded-2xl bg-white/[0.05] hover:bg-white/[0.1] text-white border border-white/[0.1] font-semibold text-xs flex items-center justify-center gap-2 active:scale-98 transition-all"
          >
            <FolderDown className="w-4 h-4 text-[#3DFFC4]" />
            <span>
              {locale === 'zh'
                ? 'GitHub Raw 链接下载 (raw.githubusercontent.com)'
                : 'Download from raw.githubusercontent.com'}
            </span>
          </button>

          {/* WebAPK / Instant Home screen PWA Option */}
          {isInstallable && (
            <button
              onClick={handleWebAPKInstall}
              className="w-full py-3 px-5 rounded-2xl bg-gradient-to-r from-blue-500/20 to-purple-500/20 hover:from-blue-500/30 hover:to-purple-500/30 text-blue-300 border border-blue-400/30 font-semibold text-xs flex items-center justify-center gap-2 active:scale-98 transition-all"
            >
              <Sparkles className="w-4 h-4 text-blue-300" />
              <span>
                {locale === 'zh'
                  ? '一键安装为手机桌面 App (WebAPK)'
                  : 'Install Instantly as Android App (WebAPK)'}
              </span>
            </button>
          )}
        </div>

        {/* 3 Step Installation Walkthrough */}
        <div className="space-y-2.5 pt-2">
          <h4 className="text-xs font-bold text-white flex items-center gap-2">
            <ShieldCheck className="w-4 h-4 text-[#3DFFC4]" />
            <span>{locale === 'zh' ? '手机安装指南' : 'Android Setup Instructions'}</span>
          </h4>

          <div className="space-y-2 text-xs">
            <div className="p-3 rounded-xl bg-white/[0.02] border border-white/[0.04] flex items-start gap-2.5">
              <span className="w-5 h-5 rounded-lg bg-[#3DFFC4]/15 text-[#3DFFC4] font-bold text-xs flex items-center justify-center shrink-0 border border-[#3DFFC4]/30">
                1
              </span>
              <span className="text-[#93A1AF] text-[11px] leading-relaxed pt-0.5">
                {locale === 'zh'
                  ? '下载完成后点击通知栏中的 APK 文件，根据系统提示选择「允许此来源的应用」。'
                  : 'After downloading, tap the .apk file in your browser downloads and allow "Install from this source".'}
              </span>
            </div>

            <div className="p-3 rounded-xl bg-white/[0.02] border border-white/[0.04] flex items-start gap-2.5">
              <span className="w-5 h-5 rounded-lg bg-[#3DFFC4]/15 text-[#3DFFC4] font-bold text-xs flex items-center justify-center shrink-0 border border-[#3DFFC4]/30">
                2
              </span>
              <span className="text-[#93A1AF] text-[11px] leading-relaxed pt-0.5">
                {locale === 'zh'
                  ? '在系统「设置 → 无障碍」中找到「Aegis / Shield 引擎」并开启。'
                  : 'Open phone Settings → Accessibility → turn ON "Aegis / Shield Engine".'}
              </span>
            </div>

            <div className="p-3 rounded-xl bg-white/[0.02] border border-white/[0.04] flex items-start gap-2.5">
              <span className="w-5 h-5 rounded-lg bg-[#3DFFC4]/15 text-[#3DFFC4] font-bold text-xs flex items-center justify-center shrink-0 border border-[#3DFFC4]/30">
                3
              </span>
              <span className="text-[#93A1AF] text-[11px] leading-relaxed pt-0.5">
                {locale === 'zh'
                  ? '使用应用内的「一键修复」免除电池优化限制，保障后台常驻。'
                  : 'Use Quick Fix inside the app to exempt Aegis from OS battery optimization killers.'}
              </span>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="pt-2 flex items-center justify-between border-t border-white/[0.06]">
          <a
            href={githubRepoUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="text-xs font-semibold text-[#93A1AF] hover:text-white flex items-center gap-1.5 transition-colors"
          >
            <FileCode className="w-3.5 h-3.5 text-[#3DFFC4]" />
            <span>GitHub Repository</span>
            <ExternalLink className="w-3 h-3" />
          </a>

          <button
            onClick={onClose}
            className="px-5 py-2 rounded-xl bg-white/[0.08] hover:bg-white/[0.15] text-white text-xs font-semibold active:scale-95 transition-all"
          >
            {locale === 'zh' ? '关闭' : 'Close'}
          </button>
        </div>
      </div>
    </div>
  );
};
