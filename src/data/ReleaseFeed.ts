import { UpdateCheckResult } from '../types';
import { UpdateRules } from './UpdateRules';

export const ReleaseFeed = {
  LATEST_RELEASE_URL: 'https://api.github.com/repos/alanhu0513-oss/aegis/releases/latest',

  parseLatest(data: any, currentVersion: string): UpdateCheckResult {
    if (!data || typeof data !== 'object') {
      return { status: 'failed', message: 'feed is not a release object' };
    }

    const tagName = data.tag_name;
    if (!tagName || typeof tagName !== 'string') {
      return { status: 'failed', message: 'feed has no tag_name' };
    }

    const releaseUrl = data.html_url || `https://github.com/alanhu0513-oss/aegis/releases/tag/${tagName}`;

    if (!UpdateRules.isNewer(tagName, currentVersion)) {
      return { status: 'up_to_date' };
    }

    return {
      status: 'available',
      info: {
        tagName,
        versionName: UpdateRules.versionNameOf(tagName),
        releaseUrl,
      },
    };
  },
};
