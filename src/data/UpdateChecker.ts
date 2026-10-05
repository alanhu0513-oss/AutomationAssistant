import { UpdateCheckResult } from '../types';
import { ReleaseFeed } from './ReleaseFeed';

export class UpdateChecker {
  private feedUrl: string;

  constructor(feedUrl: string = ReleaseFeed.LATEST_RELEASE_URL) {
    this.feedUrl = feedUrl;
  }

  public async check(currentVersion: string): Promise<UpdateCheckResult> {
    try {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 5000);

      const response = await fetch(this.feedUrl, {
        headers: {
          Accept: 'application/vnd.github+json',
        },
        signal: controller.signal,
      });

      clearTimeout(timeoutId);

      if (!response.ok) {
        return { status: 'failed', message: `HTTP ${response.status}` };
      }

      const body = await response.json();
      return ReleaseFeed.parseLatest(body, currentVersion);
    } catch (e: any) {
      return { status: 'failed', message: e?.message || 'network error' };
    }
  }
}
