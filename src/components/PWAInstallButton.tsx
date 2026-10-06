import React, { useState } from 'react';
import { Download, Smartphone, X, Sparkles } from 'lucide-react';
import { usePWAInstall } from '../hooks/usePWAInstall';
import { Locale } from '../i18n/translations';

interface PWAInstallButtonProps {
  locale: Locale;
}

export const PWAInstallButton: React.FC<PWAInstallButtonProps> = ({ locale }) => {
  const { isInstallable, isInstalled, isIOS, install } = usePWAInstall();
  const [showIOSGuide, setShowIOSGuide] = useState(false);

  // If already running as an installed standalone app, hide the button
  if (isInstalled) {
    return null;
  }

  // Chromium / Android / Desktop flow
  if (isInstallable) {
    return (
      <button
        onClick={install}
        className="px-3 py-1.5 rounded-xl bg-gradient-to-r from-[#3DFFC4]/20 to-[#2DD4BF]/20 hover:from-[#3DFFC4]/30 hover:to-[#2DD4BF]/30 text-[#3DFFC4] border border-[#3DFFC4]/40 font-bold text-xs flex items-center gap-1.5 shadow-sm shadow-[#3DFFC4]/10 active:scale-95 transition-all"
      >
        <Smartphone className="w-3.5 h-3.5" />
        <span>{locale === 'zh' ? '安装到手机' : 'Install WebAPK'}</span>
      </button>
    );
  }

  // iOS Safari flow
  if (isIOS) {
    return (
      <>
        <button
          onClick={() => setShowIOSGuide(true)}
          className="px-2.5 py-1.5 rounded-xl bg-white/[0.04] hover:bg-white/[0.08] text-white/90 border border-white/10 text-xs font-medium flex items-center gap-1.5 transition-all"
        >
          <Smartphone className="w-3.5 h-3.5 text-[#3DFFC4]" />
          <span>{locale === 'zh' ? '添加到主屏幕' : 'Add to Home'}</span>
        </button>

        {showIOSGuide && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-fade-in">
            <div className="w-full max-w-sm bg-[#0D121B] border border-white/[0.12] rounded-[24px] p-6 shadow-2xl space-y-4 text-xs">
              <div className="flex items-center justify-between">
                <h4 className="text-sm font-bold text-white flex items-center gap-2">
                  <Smartphone className="w-4 h-4 text-[#3DFFC4]" />
                  <span>{locale === 'zh' ? '在 iPhone / iPad 上安装' : 'Install on iPhone / iPad'}</span>
                </h4>
                <button
                  onClick={() => setShowIOSGuide(false)}
                  className="p-1 rounded-lg text-[#93A1AF] hover:text-white"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              <div className="space-y-2 text-[#93A1AF] leading-relaxed">
                <p>1. {locale === 'zh' ? '点击 Safari 底部工具栏的「分享」按钮。' : 'Tap the Share button in the Safari bottom toolbar.'}</p>
                <p>2. {locale === 'zh' ? '在菜单中向下滑动，点击「添加到主屏幕」。' : 'Scroll down and tap "Add to Home Screen".'}</p>
                <p>3. {locale === 'zh' ? '即可在桌面以全屏独立 App 形式使用 Aegis。' : 'Aegis will launch as a standalone app with full offline support.'}</p>
              </div>

              <button
                onClick={() => setShowIOSGuide(false)}
                className="w-full py-2.5 rounded-xl bg-white/[0.08] hover:bg-white/[0.15] text-white font-semibold text-xs active:scale-95 transition-all"
              >
                {locale === 'zh' ? '知道了' : 'Got it'}
              </button>
            </div>
          </div>
        )}
      </>
    );
  }

  return null;
};
