export interface LogEntry {
  atEpochMillis: number;
  overlayPackage: string;
  overlayLabel: string;
  gameLabel: string;
  preview?: boolean;
}

export type StrictnessLevel = 'normal' | 'gentle' | 'strict';

export interface StrictnessInfo {
  key: StrictnessLevel;
  debounceMs: number;
}

export const STRICTNESS_CONFIG: Record<StrictnessLevel, StrictnessInfo> = {
  normal: { key: 'normal', debounceMs: 400 },
  gentle: { key: 'gentle', debounceMs: 1000 },
  strict: { key: 'strict', debounceMs: 100 },
};

export const NEXT_STRICTNESS: Record<StrictnessLevel, StrictnessLevel> = {
  normal: 'gentle',
  gentle: 'strict',
  strict: 'normal',
};

export enum OemBrand {
  MIUI = 'MIUI',
  COLOROS = 'COLOROS',
  ONEUI = 'ONEUI',
  FUNTOUCH = 'FUNTOUCH',
  STOCK = 'STOCK',
}

export interface AppEntry {
  packageName: string;
  label: string;
  iconUrl?: string;
  category?: string;
  isGame?: boolean;
}

export interface CandidateApp {
  packageName: string;
  label: string;
  isSystem: boolean;
}

export enum AccessibilityWindowType {
  TYPE_APPLICATION = 1,
  TYPE_INPUT_METHOD = 2,
  TYPE_SYSTEM = 3,
  TYPE_ACCESSIBILITY_OVERLAY = 4,
  TYPE_SPLIT_SCREEN_DIVIDER = 5,
}

export interface WindowSnapshot {
  packageName: string;
  windowType: number | null;
  className: string | null;
}

export interface WindowEvent {
  type: number;
  packageName: string | null;
  className: string | null;
  windowType: number | null;
  at: number;
}

export enum EngineAction {
  NONE = 'NONE',
  FIRE = 'FIRE',
  SCHEDULE = 'SCHEDULE',
}

export interface UpdateInfo {
  tagName: string;
  versionName: string;
  releaseUrl: string;
}

export type UpdateCheckResult =
  | { status: 'available'; info: UpdateInfo }
  | { status: 'up_to_date' }
  | { status: 'failed'; message: string };

export type StatusState = 'STANDBY' | 'WAITING' | 'OPERATIONAL';
