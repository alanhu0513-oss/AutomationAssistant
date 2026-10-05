import { CandidateApp } from '../types';

export const AppSelection = {
  select(candidates: CandidateApp[], selfPackage: string): CandidateApp[] {
    return candidates
      .filter((it) => it.packageName !== selfPackage && !it.isSystem)
      .map((candidate) => {
        if (!candidate.label || candidate.label.trim().length === 0) {
          return { ...candidate, label: candidate.packageName };
        }
        return candidate;
      })
      .filter((candidate, index, self) => index === self.findIndex((t) => t.packageName === candidate.packageName))
      .sort((a, b) => a.label.toLowerCase().localeCompare(b.label.toLowerCase()));
  },
};
