package com.cinavault.android

import com.cinavault.android.data.AndroidParityStatus
import com.cinavault.android.data.WindowsBeta3Parity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowsBeta3ParityTest {

    @Test
    fun referenceIsWindowsBeta3() {
        assertEquals("v2.1.7-beta-3", WindowsBeta3Parity.REFERENCE_RELEASE)
        assertEquals("7bd0b75c", WindowsBeta3Parity.REFERENCE_MAIN)
        assertEquals("johngraven75/CinaVault-Premium", WindowsBeta3Parity.REFERENCE_REPOSITORY)
    }

    @Test
    fun everyFeatureHasUniqueIdAndNote() {
        val features = WindowsBeta3Parity.FEATURES
        assertTrue(features.isNotEmpty())
        assertEquals(features.size, features.map { it.id }.distinct().size)
        assertTrue(features.all { it.windowsFeature.isNotBlank() && it.note.isNotBlank() })
    }

    @Test
    fun vaultVpnAndDriveAreNotClaimedAsImplemented() {
        val byId = WindowsBeta3Parity.FEATURES.associateBy { it.id }
        assertEquals(AndroidParityStatus.NOT_IMPLEMENTED, byId.getValue("vault-encryption").androidStatus)
        assertEquals(AndroidParityStatus.NOT_IMPLEMENTED, byId.getValue("wireguard-vpn").androidStatus)
        assertEquals(AndroidParityStatus.NOT_IMPLEMENTED, byId.getValue("google-drive").androidStatus)
        assertEquals(AndroidParityStatus.NOT_IMPLEMENTED, byId.getValue("card-match-unmatch-lookup").androidStatus)
        assertEquals(AndroidParityStatus.NOT_IMPLEMENTED, byId.getValue("adult-gating-store-safe").androidStatus)
    }

    @Test
    fun leftBehindIsExplicitAndNonEmpty() {
        val leftBehind = WindowsBeta3Parity.leftBehind()
        assertTrue(leftBehind.isNotEmpty())
        val ids = leftBehind.map { it.id }
        assertTrue(ids.contains("vault-encryption"))
        assertTrue(ids.contains("wireguard-vpn"))
        assertTrue(ids.contains("google-drive"))
        assertTrue(leftBehind.all { it.note.contains("LEFT BEHIND") })
    }

    @Test
    fun androidGreenAloneDoesNotMakeTheSetGreen() {
        assertEquals(false, WindowsBeta3Parity.isPlatformSetGreen(allPlatformsGreen = false))
        assertEquals(true, WindowsBeta3Parity.isPlatformSetGreen(allPlatformsGreen = true))
    }

    @Test
    fun implementedFeaturesDoNotIncludeServerOnlyWork() {
        val implemented = WindowsBeta3Parity.FEATURES
            .filter { it.androidStatus == AndroidParityStatus.IMPLEMENTED }
            .map { it.id }
        assertTrue(implemented.contains("player-and-casting"))
        assertTrue(implemented.contains("spatial-dark-ui-themes"))
    }
}
