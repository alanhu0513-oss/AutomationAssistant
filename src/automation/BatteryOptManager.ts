import { OemBrand } from '../types';

export type BatteryOptStatus = 'exempt' | 'optimizing' | 'unknown';

type Listener = () => void;

class BatteryOptManagerClass {
  private static readonly STORAGE_KEY = 'aegis_battery_opt_status';
  private listeners: Set<Listener> = new Set();
  private _status: BatteryOptStatus = 'optimizing';

  constructor() {
    this.hydrate();
  }

  private hydrate() {
    try {
      const stored = localStorage.getItem(BatteryOptManagerClass.STORAGE_KEY);
      if (stored === 'exempt' || stored === 'optimizing') {
        this._status = stored;
      } else {
        // Default to 'optimizing' (needs exemption) to guide user
        this._status = 'optimizing';
      }
    } catch {
      this._status = 'optimizing';
    }
  }

  public subscribe(listener: Listener): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  private notify() {
    this.listeners.forEach((cb) => cb());
  }

  public get status(): BatteryOptStatus {
    return this._status;
  }

  public get isExempt(): boolean {
    return this._status === 'exempt';
  }

  public setStatus(newStatus: BatteryOptStatus) {
    this._status = newStatus;
    try {
      localStorage.setItem(BatteryOptManagerClass.STORAGE_KEY, newStatus);
    } catch {
      // Ignore
    }
    this.notify();
  }

  public toggleStatus() {
    this.setStatus(this._status === 'exempt' ? 'optimizing' : 'exempt');
  }

  /**
   * Generates Android System Settings Intent URI
   */
  public getSystemIntentUri(): string {
    return 'intent:#Intent;action=android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS;end';
  }

  /**
   * Generates Request Exemption Direct Intent
   */
  public getDirectRequestUri(packageName: string = 'dev.aegis.shield'): string {
    return `intent:#Intent;action=android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;data=package:${packageName};end`;
  }

  /**
   * Generates App Info Settings Intent
   */
  public getAppDetailsUri(packageName: string = 'dev.aegis.shield'): string {
    return `intent:#Intent;action=android.settings.APPLICATION_DETAILS_SETTINGS;data=package:${packageName};end`;
  }

  /**
   * Generates ADB command for power users / root / adb bridge
   */
  public getAdbCommand(packageName: string = 'dev.aegis.shield'): string {
    return `adb shell dumpsys deviceidle whitelist +${packageName}`;
  }

  /**
   * Returns manufacturer-specific direct action notes
   */
  public getOemActionGuide(brand: OemBrand) {
    switch (brand) {
      case OemBrand.MIUI:
        return {
          intent: 'intent:#Intent;component=com.miui.powerkeeper/.ui.HiddenAppsConfigActivity;end',
          actionName: 'MIUI PowerKeeper Settings',
          path: 'Security app → Battery → Battery saver → Aegis → No restrictions',
        };
      case OemBrand.COLOROS:
        return {
          intent: 'intent:#Intent;action=android.settings.APPLICATION_DETAILS_SETTINGS;data=package:dev.aegis.shield;end',
          actionName: 'ColorOS App Battery Usage',
          path: 'Settings → Apps → Aegis → Battery usage → Allow background activity',
        };
      case OemBrand.ONEUI:
        return {
          intent: 'intent:#Intent;action=android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS;end',
          actionName: 'Samsung Never Sleep Apps',
          path: 'Settings → Battery → Background usage limits → Never sleeping apps → Add Aegis',
        };
      case OemBrand.FUNTOUCH:
        return {
          intent: 'intent:#Intent;component=com.vivo.permissionmanager/.activity.PurviewTabActivity;end',
          actionName: 'vivo i Manager Autostart',
          path: 'i Manager → Power save → Background power consumption → Aegis → Allow',
        };
      case OemBrand.STOCK:
      default:
        return {
          intent: 'intent:#Intent;action=android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS;end',
          actionName: 'Android Battery Optimization',
          path: 'Settings → Apps → Aegis → Battery → Unrestricted',
        };
    }
  }
}

export const BatteryOptManager = new BatteryOptManagerClass();
