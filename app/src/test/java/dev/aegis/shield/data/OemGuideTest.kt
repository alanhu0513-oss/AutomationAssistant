package dev.aegis.shield.data

import org.junit.Assert.assertEquals
import org.junit.Test

class OemGuideTest {

    @Test
    fun `recognizes the xiaomi family`() {
        assertEquals(OemBrand.MIUI, OemGuide.brandFor("xiaomi"))
        assertEquals(OemBrand.MIUI, OemGuide.brandFor("Xiaomi"))
        assertEquals(OemBrand.MIUI, OemGuide.brandFor("REDMI"))
        assertEquals(OemBrand.MIUI, OemGuide.brandFor("Poco"))
        assertEquals(OemBrand.MIUI, OemGuide.brandFor("Black Shark"))
        assertEquals(OemBrand.MIUI, OemGuide.brandFor("blackshark"))
    }

    @Test
    fun `recognizes oppo realme and oneplus as coloros`() {
        assertEquals(OemBrand.COLOROS, OemGuide.brandFor("OPPO"))
        assertEquals(OemBrand.COLOROS, OemGuide.brandFor("realme"))
        assertEquals(OemBrand.COLOROS, OemGuide.brandFor("OnePlus"))
    }

    @Test
    fun `recognizes samsung as one ui`() {
        assertEquals(OemBrand.ONEUI, OemGuide.brandFor("samsung"))
        assertEquals(OemBrand.ONEUI, OemGuide.brandFor("Samsung"))
    }

    @Test
    fun `recognizes vivo and iqoo as funtouch`() {
        assertEquals(OemBrand.FUNTOUCH, OemGuide.brandFor("vivo"))
        assertEquals(OemBrand.FUNTOUCH, OemGuide.brandFor("VIVO"))
        assertEquals(OemBrand.FUNTOUCH, OemGuide.brandFor("iQOO"))
    }

    @Test
    fun `unknown blank and stock devices fall back to stock`() {
        assertEquals(OemBrand.STOCK, OemGuide.brandFor("Google"))
        assertEquals(OemBrand.STOCK, OemGuide.brandFor("HMD Global"))
        assertEquals(OemBrand.STOCK, OemGuide.brandFor(""))
        assertEquals(OemBrand.STOCK, OemGuide.brandFor("   "))
    }

    @Test
    fun `mapping trims whitespace before matching`() {
        assertEquals(OemBrand.ONEUI, OemGuide.brandFor("  Samsung  "))
        assertEquals(OemBrand.MIUI, OemGuide.brandFor("\tXiaomi\n"))
    }
}
