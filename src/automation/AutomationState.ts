type StateListener = () => void;

class AutomationStateManager {
  private _isRunning: boolean = false;
  private _blockedCount: number = 2; // Initial demonstration count or 0
  private _lastError: string | null = null;
  private listeners: Set<StateListener> = new Set();

  constructor() {
    try {
      const storedRunning = localStorage.getItem('aegis_engine_running');
      this._isRunning = storedRunning === null ? true : storedRunning === 'true';

      const storedCount = localStorage.getItem('aegis_blocked_count');
      if (storedCount) {
        this._blockedCount = parseInt(storedCount, 10) || 0;
      }
    } catch {
      this._isRunning = true;
    }
  }

  public subscribe(listener: StateListener): () => void {
    this.listeners.add(listener);
    return () => this.listeners.delete(listener);
  }

  private notify() {
    this.listeners.forEach((cb) => cb());
  }

  public get isRunning(): boolean {
    return this._isRunning;
  }

  public get blockedCount(): number {
    return this._blockedCount;
  }

  public get lastError(): string | null {
    return this._lastError;
  }

  public setRunning(value: boolean) {
    this._isRunning = value;
    try {
      localStorage.setItem('aegis_engine_running', String(value));
    } catch {
      // ignore
    }
    this.notify();
  }

  public recordBlocked() {
    this._blockedCount += 1;
    try {
      localStorage.setItem('aegis_blocked_count', String(this._blockedCount));
    } catch {
      // ignore
    }
    this.notify();
  }

  public publishError(message: string) {
    this._lastError = message;
    this.notify();
  }

  public clearError() {
    this._lastError = null;
    this.notify();
  }

  public reset() {
    this._isRunning = false;
    this._blockedCount = 0;
    this._lastError = null;
    this.notify();
  }
}

export const AutomationState = new AutomationStateManager();
