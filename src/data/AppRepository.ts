import { AppEntry, CandidateApp } from '../types';
import { AppSelection } from './AppSelection';

// High-fidelity pre-populated games & user apps catalog with accurate icon placeholders & package IDs
const DEFAULT_APPS: CandidateApp[] = [
  { packageName: 'com.tencent.tmgp.pubgmhd', label: 'PUBG Mobile', isSystem: false },
  { packageName: 'com.miHoYo.GenshinImpact', label: 'Genshin Impact', isSystem: false },
  { packageName: 'com.proximabeta.mf.uamo', label: 'Delta Force', isSystem: false },
  { packageName: 'com.tencent.tmgp.sgame', label: 'Honor of Kings (王者荣耀)', isSystem: false },
  { packageName: 'com.activision.callofduty.shooter', label: 'Call of Duty: Mobile', isSystem: false },
  { packageName: 'com.HoYoverse.hkrpgoversea', label: 'Honkai: Star Rail', isSystem: false },
  { packageName: 'com.riotgames.league.wildrift', label: 'League of Legends: Wild Rift', isSystem: false },
  { packageName: 'com.supercell.brawlstars', label: 'Brawl Stars', isSystem: false },
  { packageName: 'com.supercell.clashroyale', label: 'Clash Royale', isSystem: false },
  { packageName: 'com.netease.party', label: 'Eggy Party (蛋仔派对)', isSystem: false },
  { packageName: 'com.mojang.minecraftpe', label: 'Minecraft', isSystem: false },
  { packageName: 'com.roblox.client', label: 'Roblox', isSystem: false },
  { packageName: 'com.dts.freefireth', label: 'Free Fire', isSystem: false },
  { packageName: 'com.epicgames.fortnite', label: 'Fortnite Mobile', isSystem: false },
  // Filtered out candidate system apps to verify AppSelection filtering
  { packageName: 'com.android.settings', label: 'Settings', isSystem: true },
  { packageName: 'com.google.android.dialer', label: 'Phone Dialer', isSystem: true },
  { packageName: 'dev.aegis.shield', label: 'Aegis', isSystem: false },
];

export class AppRepository {
  private selfPackage: string;
  private customApps: CandidateApp[] = [];

  constructor(selfPackage: string = 'dev.aegis.shield') {
    this.selfPackage = selfPackage;
    this.loadCustomApps();
  }

  private loadCustomApps() {
    try {
      const stored = localStorage.getItem('aegis_custom_apps');
      if (stored) {
        this.customApps = JSON.parse(stored);
      }
    } catch {
      this.customApps = [];
    }
  }

  public addCustomApp(label: string, packageName: string) {
    const cleanLabel = label.trim();
    const cleanPkg = packageName.trim() || `custom.app.${Date.now()}`;
    const newApp: CandidateApp = {
      label: cleanLabel,
      packageName: cleanPkg,
      isSystem: false,
    };
    this.customApps = [newApp, ...this.customApps.filter((a) => a.packageName !== cleanPkg)];
    try {
      localStorage.setItem('aegis_custom_apps', JSON.stringify(this.customApps));
    } catch {
      // ignore
    }
  }

  public async loadUserApps(): Promise<AppEntry[]> {
    // Artificial mini-delay to replicate async load seamlessly
    await new Promise((resolve) => setTimeout(resolve, 80));

    const combined = [...this.customApps, ...DEFAULT_APPS];
    const selected = AppSelection.select(combined, this.selfPackage);

    return selected.map((candidate) => ({
      packageName: candidate.packageName,
      label: candidate.label,
      isGame: true,
    }));
  }
}
