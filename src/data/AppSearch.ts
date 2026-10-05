import { AppEntry } from '../types';

export const AppSearch = {
  normalize(query: string): string {
    return query.trim().toLowerCase();
  },

  matches(entry: AppEntry, normalizedQuery: string): boolean {
    if (!normalizedQuery) return true;
    if (entry.label.toLowerCase().startsWith(normalizedQuery)) return true;
    if (entry.packageName.toLowerCase().includes(normalizedQuery)) return true;
    return entry.label.toLowerCase().includes(normalizedQuery);
  },

  filter(apps: AppEntry[], query: string): AppEntry[] {
    const normalized = this.normalize(query);
    if (!normalized) return apps;

    const labelPrefix: AppEntry[] = [];
    const remainder: AppEntry[] = [];

    for (const app of apps) {
      if (app.label.toLowerCase().startsWith(normalized)) {
        labelPrefix.push(app);
      } else if (this.matches(app, normalized)) {
        remainder.push(app);
      }
    }

    return [...labelPrefix, ...remainder];
  },
};
