import React from 'react';
import { Bell } from 'lucide-react';
import { GlassCard } from './GlassCard';
import { translations, Locale } from '../i18n/translations';

interface NotificationPermissionDialogProps {
  locale: Locale;
  onAllow: () => void;
  onDismiss: () => void;
}

export const NotificationPermissionDialog: React.FC<NotificationPermissionDialogProps> = ({
  locale,
  onAllow,
  onDismiss,
}) => {
  const t = translations[locale];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-fade-in">
      <div className="w-full max-w-sm">
        <GlassCard borderAccent="green" className="p-6 text-center space-y-4">
          <div className="mx-auto w-14 h-14 rounded-full bg-[#3DFFC4]/15 border border-[#3DFFC4]/30 flex items-center justify-center text-[#3DFFC4]">
            <Bell className="w-7 h-7 animate-bounce" />
          </div>

          <div className="space-y-1.5">
            <h3 className="text-lg font-bold text-white">{t.notif_dialog_title}</h3>
            <p className="text-xs text-[#93A1AF] leading-relaxed">
              {t.notif_dialog_body}
            </p>
          </div>

          <div className="flex items-center justify-end gap-2 pt-2">
            <button
              onClick={onDismiss}
              className="px-4 py-2 text-xs font-semibold text-[#93A1AF] hover:text-white rounded-xl"
            >
              {t.notif_dialog_later}
            </button>
            <button
              onClick={onAllow}
              className="px-5 py-2.5 rounded-xl bg-[#3DFFC4] text-[#03261C] font-bold text-xs hover:bg-[#5EEAD4] transition-all shadow-[0_0_15px_rgba(61,255,196,0.3)]"
            >
              {t.notif_dialog_allow}
            </button>
          </div>
        </GlassCard>
      </div>
    </div>
  );
};
