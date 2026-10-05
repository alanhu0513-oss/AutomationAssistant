export const UpdateRules = {
  versionNameOf(tagName: string): string {
    return tagName.trim().replace(/^[vV]/, '');
  },

  parseVersion(raw: string): number[] {
    return this.versionNameOf(raw)
      .split('.')
      .map((part) => {
        const digits = part.match(/\d+/)?.[0];
        return digits ? parseInt(digits, 10) : 0;
      });
  },

  isNewer(candidate: string, current: string): boolean {
    const left = this.parseVersion(candidate);
    const right = this.parseVersion(current);
    const size = Math.max(left.length, right.length);

    for (let index = 0; index < size; index++) {
      const candidatePart = left[index] ?? 0;
      const currentPart = right[index] ?? 0;
      if (candidatePart !== currentPart) {
        return candidatePart > currentPart;
      }
    }

    return false;
  },
};
