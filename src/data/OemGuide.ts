import { OemBrand } from '../types';

export const OemGuide = {
  brandFor(manufacturer: string): OemBrand {
    const key = manufacturer.trim().toLowerCase().replace(/[\s-]/g, '');
    if (['xiaomi', 'redmi', 'poco', 'blackshark', 'miui', 'hyperos'].includes(key)) {
      return OemBrand.MIUI;
    }
    if (['oppo', 'realme', 'oneplus', 'coloros', 'oxygenos'].includes(key)) {
      return OemBrand.COLOROS;
    }
    if (['samsung', 'oneui', 'galaxy'].includes(key)) {
      return OemBrand.ONEUI;
    }
    if (['vivo', 'iqoo', 'funtouch', 'originos'].includes(key)) {
      return OemBrand.FUNTOUCH;
    }
    return OemBrand.STOCK;
  },

  getAllBrands(): { brand: OemBrand; labelKey: string }[] {
    return [
      { brand: OemBrand.MIUI, labelKey: 'brand_miui' },
      { brand: OemBrand.COLOROS, labelKey: 'brand_coloros' },
      { brand: OemBrand.ONEUI, labelKey: 'brand_oneui' },
      { brand: OemBrand.FUNTOUCH, labelKey: 'brand_funtouch' },
      { brand: OemBrand.STOCK, labelKey: 'brand_stock' },
    ];
  },

  getStepsForBrand(brand: OemBrand): string[] {
    switch (brand) {
      case OemBrand.MIUI:
        return ['health_miui_1', 'health_miui_2', 'health_miui_3'];
      case OemBrand.COLOROS:
        return ['health_coloros_1', 'health_coloros_2', 'health_coloros_3'];
      case OemBrand.ONEUI:
        return ['health_oneui_1', 'health_oneui_2', 'health_oneui_3'];
      case OemBrand.FUNTOUCH:
        return ['health_funtouch_1', 'health_funtouch_2', 'health_funtouch_3'];
      case OemBrand.STOCK:
      default:
        return ['health_stock_1', 'health_stock_2', 'health_stock_3'];
    }
  },
};
