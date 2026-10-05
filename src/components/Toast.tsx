import React from 'react';
import { CheckCircle2, X } from 'lucide-react';

interface ToastProps {
  message: string | null;
  onClose: () => void;
}

export const Toast: React.FC<ToastProps> = ({ message, onClose }) => {
  if (!message) return null;

  return (
    <div className="fixed bottom-6 right-6 z-50 max-w-sm w-[calc(100vw-3rem)] animate-fade-in">
      <div className="flex items-center justify-between p-4 rounded-2xl glass-panel-elevated border border-[#3DFFC4]/30 text-white shadow-2xl text-xs font-medium">
        <div className="flex items-center gap-3 min-w-0 pr-2">
          <div className="w-6 h-6 rounded-lg bg-[#3DFFC4]/15 border border-[#3DFFC4]/30 flex items-center justify-center shrink-0 text-[#3DFFC4]">
            <CheckCircle2 className="w-3.5 h-3.5" />
          </div>
          <span className="leading-snug truncate text-white/95">{message}</span>
        </div>
        <button
          onClick={onClose}
          className="text-[#93A1AF] hover:text-white p-1 transition-colors shrink-0"
        >
          <X className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
