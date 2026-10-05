import { AccessibilityWindowType } from '../types';

export const OverlayRules = {
  isTrackedEvent(type: number): boolean {
    // In Android: AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED = 32
    return type === 32 || type === 1;
  },

  /**
   * True when the window eventPackage just presented counts as a system-level layer on ANY brand.
   */
  isSystemLevel(
    windowType: number | null,
    eventPackage: string | null,
    isSystemPackage: (pkg: string) => boolean
  ): boolean {
    if (!eventPackage) return false;

    switch (windowType) {
      case AccessibilityWindowType.TYPE_SYSTEM:
      case AccessibilityWindowType.TYPE_ACCESSIBILITY_OVERLAY:
        return true;

      case AccessibilityWindowType.TYPE_INPUT_METHOD:
      case AccessibilityWindowType.TYPE_SPLIT_SCREEN_DIVIDER:
        return false;

      default:
        try {
          return isSystemPackage(eventPackage);
        } catch {
          return false;
        }
    }
  },

  /**
   * Which events may re-anchor "what app is currently in front".
   */
  shouldAdoptForeground(
    packageName: string | null,
    selfPackage: string,
    homePackages: Set<string>,
    windowType: number | null,
    isSystemPackage: (pkg: string) => boolean
  ): boolean {
    if (!packageName) return false;
    if (packageName === selfPackage) return false;
    if (homePackages.has(packageName)) return true;

    if (
      windowType === AccessibilityWindowType.TYPE_INPUT_METHOD ||
      windowType === AccessibilityWindowType.TYPE_SPLIT_SCREEN_DIVIDER
    ) {
      return false;
    }

    return !this.isSystemLevel(windowType, packageName, isSystemPackage);
  },

  /**
   * The core trigger: press BACK when a system-level window appears while inside a protected game.
   */
  shouldDismiss(
    foregroundPackage: string | null,
    eventPackage: string | null,
    windowType: number | null,
    protectedApps: Set<string>,
    selfPackage: string,
    homePackages: Set<string>,
    exemptPackages: Set<string>,
    isSystemPackage: (pkg: string) => boolean
  ): boolean {
    if (!foregroundPackage) return false;
    if (!protectedApps.has(foregroundPackage)) return false;
    if (!eventPackage) return false;
    if (eventPackage === foregroundPackage || eventPackage === selfPackage) return false;
    if (homePackages.has(eventPackage) || exemptPackages.has(eventPackage)) return false;

    return this.isSystemLevel(windowType, eventPackage, isSystemPackage);
  },

  shouldDispatch(now: number, lastDispatchAt: number, windowMs: number): boolean {
    return now - lastDispatchAt >= windowMs;
  },
};
