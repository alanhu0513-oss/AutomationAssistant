import { OemBrand, StrictnessLevel } from '../types';
import { Locale } from '../i18n/translations';

type Listener = () => void;

class TargetStoreManager {
  private static readonly STORAGE_PREFIX = 'aegis_';
  private listeners: Set<Listener> = new Set();

  private _protectedApps: Set<string> = new Set();
  private _isFirstLaunch: boolean = false;
  private _notificationsPrompted: boolean = false;
  private _serviceEverEnabled: boolean = false;
  private _strictnessLevels: Map<string, StrictnessLevel> = new Map();
  private _previewMode: boolean = false;
  private _selectedBrand: OemBrand = OemBrand.MIUI;
  private _locale: Locale = 'en';

  constructor() {
    this.hydrate();
  }

  private hydrate() {
    try {
      // Default to true if not set before
      const firstLaunchRaw = localStorage.getItem(`${TargetStoreManager.STORAGE_PREFIX}is_first_launch`);
      this._isFirstLaunch = firstLaunchRaw === null ? true : firstLaunchRaw === 'true';

      const notifPromptedRaw = localStorage.getItem(`${TargetStoreManager.STORAGE_PREFIX}notifications_prompted`);
      this._notificationsPrompted = notifPromptedRaw === 'true';

      const serviceEverEnabledRaw = localStorage.getItem(`${TargetStoreManager.STORAGE_PREFIX}service_ever_enabled`);
      this._serviceEverEnabled = serviceEverEnabledRaw === 'true';

      const previewRaw = localStorage.getItem(`${TargetStoreManager.STORAGE_PREFIX}preview_mode`);
      this._previewMode = previewRaw === 'true';

      const brandRaw = localStorage.getItem(`${TargetStoreManager.STORAGE_PREFIX}selected_brand`);
      if (brandRaw && Object.values(OemBrand).includes(brandRaw as OemBrand)) {
        this._selectedBrand = brandRaw as OemBrand;
      }

      const localeRaw = localStorage.getItem(`${TargetStoreManager.STORAGE_PREFIX}locale`);
      if (localeRaw === 'en' || localeRaw === 'zh') {
        this._locale = localeRaw;
      } else {
        // Auto detect browser language
        const navLang = navigator.language?.toLowerCase() || '';
        this._locale = navLang.startsWith('zh') ? 'zh' : 'en';
      }

      const protectedRaw = localStorage.getItem(`${TargetStoreManager.STORAGE_PREFIX}protected_apps`);
      if (protectedRaw) {
        const parsed = JSON.parse(protectedRaw);
        if (Array.isArray(parsed)) {
          this._protectedApps = new Set(parsed);
        }
      } else {
        // Seed default popular game selections for instant satisfaction
        this._protectedApps = new Set(['com.tencent.tmgp.pubgmhd', 'com.miHoYo.GenshinImpact']);
      }

      const strictnessRaw = localStorage.getItem(`${TargetStoreManager.STORAGE_PREFIX}strictness_levels`);
      if (strictnessRaw) {
        const parsed = JSON.parse(strictnessRaw);
        if (parsed && typeof parsed === 'object') {
          this._strictnessLevels = new Map(Object.entries(parsed));
        }
      }
    } catch (e) {
      console.error('TargetStore hydrate error:', e);
    }
  }

  public subscribe(listener: Listener): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  private notify() {
    this.listeners.forEach((cb) => cb());
  }

  public get protectedApps(): Set<string> {
    return new Set(this._protectedApps);
  }

  public get isFirstLaunch(): boolean {
    return this._isFirstLaunch;
  }

  public get notificationsPrompted(): boolean {
    return this._notificationsPrompted;
  }

  public get serviceEverEnabled(): boolean {
    return this._serviceEverEnabled;
  }

  public get previewMode(): boolean {
    return this._previewMode;
  }

  public get selectedBrand(): OemBrand {
    return this._selectedBrand;
  }

  public get locale(): Locale {
    return this._locale;
  }

  public get strictnessLevels(): Map<string, StrictnessLevel> {
    return new Map(this._strictnessLevels);
  }

  public setAppProtected(packageName: string, isProtected: boolean) {
    if (isProtected) {
      this._protectedApps.add(packageName);
    } else {
      this._protectedApps.delete(packageName);
    }
    localStorage.setItem(
      `${TargetStoreManager.STORAGE_PREFIX}protected_apps`,
      JSON.stringify(Array.from(this._protectedApps))
    );
    this.notify();
  }

  public completeFirstLaunch() {
    this._isFirstLaunch = false;
    localStorage.setItem(`${TargetStoreManager.STORAGE_PREFIX}is_first_launch`, 'false');
    this.notify();
  }

  public resetFirstLaunch() {
    this._isFirstLaunch = true;
    localStorage.setItem(`${TargetStoreManager.STORAGE_PREFIX}is_first_launch`, 'true');
    this.notify();
  }

  public markNotificationsPrompted() {
    this._notificationsPrompted = true;
    localStorage.setItem(`${TargetStoreManager.STORAGE_PREFIX}notifications_prompted`, 'true');
    this.notify();
  }

  public markServiceEverEnabled() {
    this._serviceEverEnabled = true;
    localStorage.setItem(`${TargetStoreManager.STORAGE_PREFIX}service_ever_enabled`, 'true');
    this.notify();
  }

  public strictnessFor(packageName: string | null): StrictnessLevel {
    if (!packageName) return 'normal';
    return this._strictnessLevels.get(packageName) || 'normal';
  }

  public setStrictness(packageName: string, level: StrictnessLevel) {
    this._strictnessLevels.set(packageName, level);
    const obj = Object.fromEntries(this._strictnessLevels);
    localStorage.setItem(`${TargetStoreManager.STORAGE_PREFIX}strictness_levels`, JSON.stringify(obj));
    this.notify();
  }

  public setPreviewMode(enabled: boolean) {
    this._previewMode = enabled;
    localStorage.setItem(`${TargetStoreManager.STORAGE_PREFIX}preview_mode`, String(enabled));
    this.notify();
  }

  public setSelectedBrand(brand: OemBrand) {
    this._selectedBrand = brand;
    localStorage.setItem(`${TargetStoreManager.STORAGE_PREFIX}selected_brand`, brand);
    this.notify();
  }

  public setLocale(locale: Locale) {
    this._locale = locale;
    localStorage.setItem(`${TargetStoreManager.STORAGE_PREFIX}locale`, locale);
    this.notify();
  }
}

export const TargetStore = new TargetStoreManager();
