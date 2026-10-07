import React, { useState } from 'react';
import {
  Download,
  Smartphone,
  ShieldCheck,
  QrCode,
  CheckCircle2,
  Sparkles,
  ExternalLink,
  ChevronRight,
  Info,
} from 'lucide-react';
import { GlassCard } from './GlassCard';
import { triggerApkDownload } from '../automation/ApkDownloader';
import { usePWAInstall } from '../hooks/usePWAInstall';
import { Locale, translations } from '../i18n/translations';

interface DirectApkDownloadCardProps {
  locale: Locale;
  onShowToast: (msg: string) => void;
}

export const DirectApkDownloadCard: React.FC<DirectApkDownloadCardProps> = ({
  locale,
  onShowToast,
}) => {
  const [downloaded, setDownloaded] = useState(false);
  const [showQr, setShowQr] = useState(false);
  const { isInstallable, isInstalled, install } = usePWAInstall();
  const currentUrl = window.location.href;

  const handleDownload = () => {
    triggerApkDownload('aegis-shield-v2.2.0.apk');
    setDownloaded(true);
    onShowToast(
      locale === 'zh'
        ? '已成功触发 APK 安装包下载！'
        : 'Starting download of aegis-shield-v2.2.0.apk!'
    );
    setTimeout(() => setDownloaded(false), 4000);
  };

  const handleWebAPK = async () => {
    if (install) {
      const ok = await install();
      if (ok) {
        onShowToast(locale === 'zh' ? '应用正在安装到桌面…' : 'Installing app to home screen…');
      }
    }
  };

  return (
    <GlassCard borderAccent="green" className="p-6 sm:p-7 relative overflow-hidden group">
      {/* Glow Effect */}
      <div className="absolute -top-12 -right-12 w-48 h-48 bg-[#3DFFC4]/10 rounded-full blur-2xl pointer-events-none" />

      <div className="relative z-10 space-y-5">
        {/* Top Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-white/[0.08] pb-4">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-[#3DFFC4]/20 to-[#2DD4BF]/10 border border-[#3DFFC4]/40 flex items-center justify-center text-[#3DFFC4] shadow-[0_0_20px_rgba(61,255,196,0.15)] shrink-0">
              <Smartphone className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="text-base font-bold text-white tracking-tight">
                  {locale === 'zh' ? '安装原生 Android App' : 'Download Android App (.APK)'}
                </h3>
                <span className="text-[10px] font-mono font-bold px-2 py-0.5 rounded-full bg-[#3DFFC4]/15 text-[#3DFFC4] border border-[#3DFFC4]/30">
                  v2.2.0 APK
                </span>
              </div>
              <p className="text-xs text-[#93A1AF]">
                {locale === 'zh'
                  ? '一键直接下载 APK 安装包并在手机上开启守护'
                  : 'Tap below to download and install directly on your Android phone'}
              </p>
            </div>
          </div>

          {/* Quick QR code toggle on desktop */}
          <button
            onClick={() => setShowQr(!showQr)}
            className="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-white/[0.04] hover:bg-white/[0.08] text-xs font-semibold text-[#93A1AF] hover:text-white border border-white/10 transition-all active:scale-95"
          >
            <QrCode className="w-3.5 h-3.5 text-[#3DFFC4]" />
            <span>{showQr ? (locale === 'zh' ? '隐藏二维码' : 'Hide QR') : (locale === 'zh' ? '手机扫码安装' : 'Scan with Phone')}</span>
          </button>
        </div>

        {/* QR Code expansion for Desktop users */}
        {showQr && (
          <div className="p-4 rounded-2xl bg-[#090D14] border border-white/[0.08] flex flex-col sm:flex-row items-center gap-4 animate-fade-in">
            <div className="w-32 h-32 p-2 bg-white rounded-xl flex items-center justify-center shrink-0 shadow-lg">
              <img
                src={`https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${encodeURIComponent(
                  currentUrl
                )}`}
                alt="Scan to open on phone"
                className="w-full h-full object-contain"
              />
            </div>
            <div className="text-xs space-y-1.5 text-center sm:text-left">
              <span className="font-bold text-white block">
                {locale === 'zh' ? '用手机相机扫描二维码' : 'Scan with your Phone Camera'}
              </span>
              <p className="text-[#93A1AF] text-[11px] leading-relaxed">
                {locale === 'zh'
                  ? '使用手机自带相机或浏览器扫描此码，在手机上直接下载 APK 或添加为桌面独立 App。'
                  : 'Point your Android camera here to open and download the APK directly onto your device.'}
              </p>
              <span className="text-[10px] font-mono text-[#3DFFC4] truncate block">
                {currentUrl}
              </span>
            </div>
          </div>
        )}

        {/* Primary Action Button Bar */}
        <div className="grid grid-cols-1 sm:grid-cols-12 gap-3">
          {/* Main 1-Click APK Download Button */}
          <button
            onClick={handleDownload}
            className={`sm:col-span-8 py-4 px-6 rounded-2xl font-extrabold text-sm flex items-center justify-center gap-3 transition-all duration-300 shadow-xl active:scale-98 cursor-pointer ${
              downloaded
                ? 'bg-[#10B981] text-white shadow-[#10B981]/30'
                : 'bg-[#3DFFC4] hover:bg-[#5EEAD4] text-[#03261C] shadow-[0_0_25px_rgba(61,255,196,0.3)]'
            }`}
          >
            {downloaded ? (
              <>
                <CheckCircle2 className="w-5 h-5 stroke-[2.5]" />
                <span>{locale === 'zh' ? '已成功下载 aegis-shield-v2.2.0.apk！' : 'Downloaded aegis-shield-v2.2.0.apk!'}</span>
              </>
            ) : (
              <>
                <Download className="w-5 h-5 stroke-[2.5]" />
                <span>
                  {locale === 'zh'
                    ? '立即下载 Aegis.apk (Android 安装包)'
                    : 'Download Aegis.apk (Android Package)'}
                </span>
              </>
            )}
          </button>

          {/* WebAPK / PWA Button if supported */}
          {isInstallable ? (
            <button
              onClick={handleWebAPK}
              className="sm:col-span-4 py-4 px-4 rounded-2xl bg-gradient-to-r from-blue-600/30 to-purple-600/30 hover:from-blue-600/40 hover:to-purple-600/40 text-white font-bold text-xs flex items-center justify-center gap-2 border border-blue-400/30 active:scale-98 transition-all"
            >
              <Sparkles className="w-4 h-4 text-blue-300" />
              <span>{locale === 'zh' ? '安装为桌面 App' : 'Install to Home'}</span>
            </button>
          ) : (
            <button
              onClick={handleDownload}
              className="sm:col-span-4 py-4 px-4 rounded-2xl bg-white/[0.04] hover:bg-white/[0.08] text-white/90 font-semibold text-xs flex items-center justify-center gap-2 border border-white/10 active:scale-98 transition-all"
            >
              <Download className="w-4 h-4 text-[#3DFFC4]" />
              <span>{locale === 'zh' ? '直接下载 APK' : 'Direct APK'}</span>
            </button>
          )}
        </div>

        {/* Quick 3-Step Mini Badge Line */}
        <div className="pt-2 border-t border-white/[0.06] flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 text-[11px] text-[#93A1AF]">
          <span className="flex items-center gap-1.5">
            <ShieldCheck className="w-3.5 h-3.5 text-[#3DFFC4]" />
            {locale === 'zh'
              ? '下载后点击安装 ➔ 允许未知来源 ➔ 在无障碍设置中开启 Aegis'
              : 'Tap to install ➔ Allow unknown sources ➔ Turn ON in Accessibility'}
          </span>

          <span className="font-mono text-[10px] text-[#3DFFC4]">
            Android 10+ · Zero Root
          </span>
        </div>
      </div>
    </GlassCard>
  );
};
