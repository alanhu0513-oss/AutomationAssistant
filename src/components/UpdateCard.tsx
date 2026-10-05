import React from 'react';
import { Sparkles, ExternalLink } from 'lucide-react';
import { UpdateInfo } from '../types';
import { translations, Locale } from '../i18n/translations';

interface UpdateCardProps {
  info: UpdateInfo;
  locale: Locale;
  onOpenRelease: (url: string) => void;
}

export const UpdateCard: React.FC<UpdateCardProps> = ({ info, locale, onOpenRelease }) => {
  const t = translations[locale];

  return (
    <div className="w-full rounded-[24px] p-5 bg-[#19212B] border border-[#3DFFC4]/30 shadow-[0_0_20px_rgba(61,255,196,0.08)] space-y-3">
      <div className="flex items-center gap-2 text-white font-semibold text-sm">
        <Sparkles className="w-4 h-4 text-[#3DFFC4]" />
        <h4>{t.update_title}</h4>
      </div>

      <p className="text-xs text-[#93A1AF] leading-relaxed">
        {t.update_body(info.versionName)}
      </p>

      <button
        onClick={() => onOpenRelease(info.releaseUrl)}
        className="w-full py-2.5 px-4 rounded-xl bg-[#1F2833] hover:bg-[#3DFFC4]/15 text-[#3DFFC4] border border-[#3DFFC4]/30 text-xs font-bold transition-all flex items-center justify-center gap-1.5"
      >
        <span>{t.update_action}</span>
        <ExternalLink className="w-3.5 h-3.5" />
      </button>
    </div>
  );
};
