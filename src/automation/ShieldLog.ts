import { LogEntry } from '../types';

type LogListener = (entries: LogEntry[]) => void;

class ShieldLogManager {
  private static readonly KEY_SHIELD_LOG = 'aegis_shield_log';
  public static readonly MAX_ENTRIES = 100;

  private entries: LogEntry[] = [];
  private listeners: Set<LogListener> = new Set();

  constructor() {
    this.hydrate();
  }

  private hydrate() {
    try {
      const raw = localStorage.getItem(ShieldLogManager.KEY_SHIELD_LOG);
      if (raw) {
        this.entries = this.decode(raw);
      } else {
        // Initial sample history to show how proof-of-work appears
        this.entries = [
          {
            atEpochMillis: Date.now() - 1000 * 60 * 12,
            overlayPackage: 'com.miui.powerkeeper',
            overlayLabel: 'MIUI Time Limit Dialog',
            gameLabel: 'PUBG Mobile',
            preview: false,
          },
          {
            atEpochMillis: Date.now() - 1000 * 60 * 45,
            overlayPackage: 'com.vivo.permissionmanager',
            overlayLabel: 'Vivo System Popup',
            gameLabel: 'Genshin Impact',
            preview: false,
          },
        ];
      }
    } catch (e) {
      console.error('ShieldLog hydrate failed:', e);
      this.entries = [];
    }
  }

  public subscribe(listener: LogListener): () => void {
    this.listeners.add(listener);
    listener([...this.entries]);
    return () => this.listeners.delete(listener);
  }

  private notify() {
    const copy = [...this.entries];
    this.listeners.forEach((cb) => cb(copy));
  }

  public getEntries(): LogEntry[] {
    return [...this.entries];
  }

  public record(entry: LogEntry) {
    const next = [entry, ...this.entries].slice(0, ShieldLogManager.MAX_ENTRIES);
    this.entries = next;
    try {
      localStorage.setItem(ShieldLogManager.KEY_SHIELD_LOG, this.encode(next));
    } catch (e) {
      console.error('Failed to save ShieldLog:', e);
    }
    this.notify();
  }

  public clear() {
    this.entries = [];
    localStorage.removeItem(ShieldLogManager.KEY_SHIELD_LOG);
    this.notify();
  }

  public encode(list: LogEntry[]): string {
    return JSON.stringify(list);
  }

  public decode(raw: string): LogEntry[] {
    try {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) {
        return parsed.filter((item) => item && typeof item.atEpochMillis === 'number');
      }
      return [];
    } catch {
      return [];
    }
  }
}

export const ShieldLog = new ShieldLogManager();
