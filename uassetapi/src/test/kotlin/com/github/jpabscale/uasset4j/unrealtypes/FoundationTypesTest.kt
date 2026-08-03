// Copyright (c) 2026 jpabscale — original tests (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j.unrealtypes

import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.customversions.FFortniteMainBranchObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion.VER_UE4_AUTOMATIC_VERSION
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion.VER_UE4_AUTOMATIC_VERSION_PLUS_ONE
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion.VER_UE4_OLDEST_LOADABLE_PACKAGE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class FStringTest {
    @Test
    fun asciiDefaultsToUtf8() {
        val s = FString("hello")
        assertEquals(Charsets.UTF_8, s.Encoding)
        assertEquals("hello", s.Value)
    }

    @Test
    fun nonAsciiDefaultsToUnicode() {
        val s = FString("日本語")
        assertEquals(Charsets.UTF_16LE, s.Encoding)
    }

    @Test
    fun nullCaseHandling() {
        assertNull(FString.FromString(FString.NullCase))
        assertNull(FString.FromString(null))
        assertEquals("日本語", FString.FromString("日本語")?.Value)
        assertEquals(FString.NullCase, FString("").apply { Value = null }.toString())
    }

    @Test
    fun equality() {
        assertEquals(FString("abc"), FString("abc"))
        assertEquals(FString("abc"), "abc")
        assertFalse(FString("abc") == FString("abd"))
    }
}

class FGuidTest {
    private val blueprintsGuid = FGuid.fromUnsignedInts(0xB0D832E4u, 0x1F894F0Du, 0xACCF7EB7u, 0x36FD4AA2u)

    @Test
    fun netByteLayout() {
        // UAPUtils.GUID(v1,v2,v3,v4) builds bytes = [v1 LE][v2 LE][v3 LE][v4 LE] then new Guid(bytes).
        // For (0xB0D832E4, 0x1F894F0D, 0xACCF7EB7, 0x36FD4AA2) that Guid's ToByteArray() is:
        val expected = "E432D8B00D4F891FB77ECFACA24AFD36"
        assertEquals(
            expected.chunked(2).map { it.toInt(16).toByte() },
            blueprintsGuid.toByteArray().toList(),
        )
    }

    @Test
    fun prettyString() {
        // UAPUtils.ConvertToString reverses Guid.ToByteArray() before formatting.
        assertEquals("{B0D832E4-1F89-4F0D-ACCF-7EB736FD4AA2}", blueprintsGuid.toPrettyString())
    }

    @Test
    fun uuidRoundTrip() {
        val uuid = UUID.fromString("B0D832E4-4F0D-1F89-B77E-CFACA24AFD36")
        assertEquals(blueprintsGuid, FGuid.fromUuid(uuid))
        assertEquals(uuid, blueprintsGuid.toUuid())
    }

    @Test
    fun byteRoundTrip() {
        val decoded = FGuid.fromBytes(blueprintsGuid.toByteArray())
        assertEquals(blueprintsGuid, decoded)
        assertEquals(blueprintsGuid.data1, decoded.data1)
        assertEquals(blueprintsGuid.data2, decoded.data2)
        assertEquals(blueprintsGuid.data3, decoded.data3)
        assertEquals(blueprintsGuid.data4, decoded.data4)
    }
}

class ObjectVersionTest {
    @Test
    fun valuesMatchCSharp() {
        assertEquals(0, ObjectVersion.UNKNOWN.value)
        assertEquals(214, VER_UE4_OLDEST_LOADABLE_PACKAGE.value)
        assertEquals(VER_UE4_AUTOMATIC_VERSION_PLUS_ONE.value - 1, VER_UE4_AUTOMATIC_VERSION.value)
        assertEquals(1000, ObjectVersionUE5.INITIAL_VERSION.value)
        assertEquals(ObjectVersionUE5.AUTOMATIC_VERSION_PLUS_ONE.value - 1, ObjectVersionUE5.AUTOMATIC_VERSION.value)
    }

    @Test
    fun customVersionEnumsPreserveIntValues() {
        assertEquals(
            FFortniteMainBranchObjectVersion.VersionPlusOne.value - 1,
            FFortniteMainBranchObjectVersion.LatestVersion.value,
        )
        assertEquals(0, FFortniteMainBranchObjectVersion.BeforeCustomVersionWasAdded.value)
    }
}

class CustomVersionTest {
    @Test
    fun friendlyNameLookup() {
        val guid = FGuid.fromUnsignedInts(0xB0D832E4u, 0x1F894F0Du, 0xACCF7EB7u, 0x36FD4AA2u)
        assertEquals("FBlueprintsObjectVersion", CustomVersion.GetCustomVersionFriendlyNameFromGuid(guid))
        assertEquals(guid, CustomVersion.GetCustomVersionGuidFromFriendlyName("FBlueprintsObjectVersion"))
    }

    @Test
    fun unusedKey() {
        assertEquals(
            "UnusedCustomVersionKey",
            CustomVersion.GetCustomVersionFriendlyNameFromGuid(CustomVersion.UnusedCustomVersionKey),
        )
        assertEquals(
            CustomVersion.UnusedCustomVersionKey,
            CustomVersion.GetCustomVersionGuidFromFriendlyName("UnusedCustomVersionKey"),
        )
    }

    @Test
    fun constructorByFriendlyName() {
        val cv = CustomVersion("FReleaseObjectVersion", 12)
        assertEquals("FReleaseObjectVersion", cv.FriendlyName)
        assertEquals(12, cv.Version)
    }
}

class FNameTest {
    @Test
    fun toStringWithNumber() {
        val asset = com.github.jpabscale.uasset4j.UAsset()
        val name = FName(asset, "Bone", 2)
        assertEquals("Bone_1", name.toString())
    }

    @Test
    fun fromStringParsesNumberSuffix() {
        val asset = com.github.jpabscale.uasset4j.UAsset()
        asset.AddNameReference(com.github.jpabscale.uasset4j.unrealtypes.FString("Bone"))
        val parsed = FName.FromString(asset, "Bone_3")
        assertEquals("Bone", parsed?.Value?.Value)
        assertEquals(4, parsed?.Number)
        assertEquals("Bone_3", parsed.toString())
    }

    @Test
    fun dummyFName() {
        val dummy = FName.DefineDummy(null, "SomeName", 5)
        assertTrue(dummy.IsDummy)
        assertEquals("SomeName", dummy.Value?.Value)
        assertEquals("SomeName_4", dummy.toString())
    }

    @Test
    fun equalityByStringWhenAssetsDiffer() {
        val a1 = com.github.jpabscale.uasset4j.UAsset()
        val a2 = com.github.jpabscale.uasset4j.UAsset()
        a1.AddNameReference(com.github.jpabscale.uasset4j.unrealtypes.FString("Same"))
        a2.AddNameReference(com.github.jpabscale.uasset4j.unrealtypes.FString("Same"))
        assertEquals(FName(a1, "Same"), FName(a2, "Same"))
    }
}

class FPackageIndexTest {
    @Test
    fun importExportConversion() {
        val imp = FPackageIndex.FromImport(0)
        assertEquals(-1, imp.Index)
        assertTrue(imp.IsImport())
        assertFalse(imp.IsExport())

        val exp = FPackageIndex.FromExport(0)
        assertEquals(1, exp.Index)
        assertTrue(exp.IsExport())
        assertFalse(exp.IsImport())

        assertTrue(FPackageIndex(0).IsNull())
    }

    @Test
    fun fromRawIndexAndEquality() {
        assertEquals(FPackageIndex(5), FPackageIndex.FromRawIndex(5))
        assertEquals(0, FPackageIndex(3).compareTo(FPackageIndex(3)))
        assertTrue(FPackageIndex(2).compareTo(FPackageIndex(9)) < 0)
    }
}
