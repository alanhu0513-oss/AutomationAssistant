import { EngineAction, WindowEvent, WindowSnapshot } from '../types';
import { OverlayRules } from './OverlayRules';

export class OverlayEngine {
  public lastWindow: WindowSnapshot | null = null;
  public foregroundPackage: string | null = null;
  public lastFiredAt: number = 0;
  public scheduledAt: number | null = null;
  private firedOnce: boolean = false;

  public static readonly DEFAULT_DEBOUNCE_MS = 400;

  constructor(
    private selfPackage: string,
    private homePackages: Set<string>,
    private exemptPackages: Set<string>,
    private isSystemPackage: (pkg: string) => boolean,
    private debounceFor: (foreground: string | null) => number = () => OverlayEngine.DEFAULT_DEBOUNCE_MS
  ) {}

  public onEvent(event: WindowEvent, protectedApps: Set<string>): EngineAction {
    if (!OverlayRules.isTrackedEvent(event.type)) return EngineAction.NONE;
    const packageName = event.packageName;
    if (!packageName) return EngineAction.NONE;

    this.lastWindow = {
      packageName,
      windowType: event.windowType,
      className: event.className,
    };

    // Decide dismissal against foreground BEFORE this event
    const shouldDismiss = OverlayRules.shouldDismiss(
      this.foregroundPackage,
      packageName,
      event.windowType,
      protectedApps,
      this.selfPackage,
      this.homePackages,
      this.exemptPackages,
      this.isSystemPackage
    );

    if (
      OverlayRules.shouldAdoptForeground(
        packageName,
        this.selfPackage,
        this.homePackages,
        event.windowType,
        this.isSystemPackage
      )
    ) {
      this.foregroundPackage = packageName;
    }

    if (!shouldDismiss) return EngineAction.NONE;
    return this.resolveDispatch(event.at);
  }

  public shouldDismissNow(protectedApps: Set<string>): boolean {
    const window = this.lastWindow;
    if (!window) return false;

    return OverlayRules.shouldDismiss(
      this.foregroundPackage,
      window.packageName,
      window.windowType,
      protectedApps,
      this.selfPackage,
      this.homePackages,
      this.exemptPackages,
      this.isSystemPackage
    );
  }

  public onFired(now: number) {
    this.firedOnce = true;
    this.lastFiredAt = now;
    this.scheduledAt = null;
  }

  public cancelScheduled() {
    this.scheduledAt = null;
  }

  private resolveDispatch(now: number): EngineAction {
    if (this.scheduledAt !== null) return EngineAction.NONE;
    const debounce = Math.max(0, this.debounceFor(this.foregroundPackage));

    if (this.firedOnce && !OverlayRules.shouldDispatch(now, this.lastFiredAt, debounce)) {
      const fireAt = this.lastFiredAt + debounce;
      this.scheduledAt = fireAt;
      return EngineAction.SCHEDULE;
    }

    this.lastFiredAt = now;
    this.firedOnce = true;
    return EngineAction.FIRE;
  }
}
