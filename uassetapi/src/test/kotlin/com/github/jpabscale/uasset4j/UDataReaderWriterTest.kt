// Copyright (c) 2026 jpabscale — original tests (not part of the UAssetAPI port)
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unversioned.Usmap
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UDataReaderWriterTest {
    @Test
    fun primitiveRoundTrip() {
        val w = UnrealBinaryWriter()
        w.WriteInt16(0x1234.toShort())
        w.WriteUInt16(0xABCD)
        w.WriteInt32(-123456789)
        w.WriteUInt32(0xFEDCBA98L)
        w.WriteInt64(-987654321987654321L)
        w.WriteUInt64(0x123456789ABCDEF0L)
        w.WriteSingle(3.14f)
        w.WriteDouble(2.718281828)
        w.WriteBooleanByte(true)
        w.WriteBooleanInt(false)
        w.WriteByte(0xFF)

        val r = UnrealBinaryReader(w.toByteArray())
        assertEquals(0x1234.toShort(), r.ReadInt16())
        assertEquals(0xABCD, r.ReadUInt16())
        assertEquals(-123456789, r.ReadInt32())
        assertEquals(0xFEDCBA98L, r.ReadUInt32())
        assertEquals(-987654321987654321L, r.ReadInt64())
        assertEquals(0x123456789ABCDEF0L, r.ReadUInt64())
        assertEquals(3.14f, r.ReadSingle())
        assertEquals(2.718281828, r.ReadDouble())
        assertTrue(r.ReadBooleanByte())
        assertFalse(r.ReadBooleanInt())
        assertEquals(0xFF, r.ReadByte())
    }

    @Test
    fun fstringUtf8RoundTrip() {
        val w = UnrealBinaryWriter()
        w.Write(FString("Hello, Unreal!"))
        val r = UnrealBinaryReader(w.toByteArray())
        val s = r.ReadFString()
        assertEquals("Hello, Unreal!", s?.Value)
        assertEquals(Charsets.UTF_8, s?.Encoding)
    }

    @Test
    fun fstringUnicodeRoundTrip() {
        val w = UnrealBinaryWriter()
        w.Write(FString("日本語テキスト"))
        val r = UnrealBinaryReader(w.toByteArray())
        val s = r.ReadFString()
        assertEquals("日本語テキスト", s?.Value)
        assertEquals(Charsets.UTF_16LE, s?.Encoding)
    }

    @Test
    fun fstringNullRoundTrip() {
        val w = UnrealBinaryWriter()
        w.Write(null)
        w.Write(FString(null))
        val r = UnrealBinaryReader(w.toByteArray())
        assertNull(r.ReadFString())
        assertNull(r.ReadFString())
    }

    @Test
    fun utf8StringRoundTrip() {
        val w = UnrealBinaryWriter()
        w.WriteUtf8String(FString("no null terminator"))
        val r = UnrealBinaryReader(w.toByteArray())
        assertEquals("no null terminator", r.ReadUtf8String()?.Value)
    }

    @Test
    fun customVersionContainerGuidsRoundTrip() {
        val container = listOf(
            CustomVersion("FReleaseObjectVersion", 5),
            CustomVersion("FAnimObjectVersion", 3),
            CustomVersion("FUnusedFake", 0), // version <= 0 -> skipped
            CustomVersion(CustomVersion.UnusedCustomVersionKey, 7), // unused key -> skipped
        )
        val w = UnrealBinaryWriter()
        w.WriteCustomVersionContainer(ECustomVersionSerializationFormat.Guids, container)
        val r = UnrealBinaryReader(w.toByteArray())
        val out = r.ReadCustomVersionContainer(ECustomVersionSerializationFormat.Guids)

        assertEquals(2, out.size)
        assertEquals("FReleaseObjectVersion", CustomVersion.GetCustomVersionFriendlyNameFromGuid(out[0].Key))
        assertEquals(5, out[0].Version)
        assertEquals("FAnimObjectVersion", CustomVersion.GetCustomVersionFriendlyNameFromGuid(out[1].Key))
        assertEquals(3, out[1].Version)
        assertEquals(r.length, r.position)
    }

    @Test
    fun customVersionContainerOptimizedRoundTrip() {
        val container = listOf(
            CustomVersion("FReleaseObjectVersion", 5),
            CustomVersion("FAnimObjectVersion", 3),
        )
        val w = UnrealBinaryWriter()
        w.WriteCustomVersionContainer(ECustomVersionSerializationFormat.Optimized, container)
        val r = UnrealBinaryReader(w.toByteArray())
        val out = r.ReadCustomVersionContainer(ECustomVersionSerializationFormat.Optimized)

        assertEquals(2, out.size)
        assertEquals(5, out[0].Version)
        assertEquals(3, out[1].Version)
    }

    @Test
    fun customVersionContainerMergesMappingsAndOld() {
        val w = UnrealBinaryWriter()
        w.WriteCustomVersionContainer(
            ECustomVersionSerializationFormat.Guids,
            listOf(CustomVersion("FReleaseObjectVersion", 5)),
        )
        val r = UnrealBinaryReader(w.toByteArray())

        val usmap = Usmap()
        usmap.CustomVersionContainer = mutableListOf(CustomVersion("FAnimObjectVersion", 9))
        val oldContainer = listOf(CustomVersion("FAnimPhysObjectVersion", 2))

        val out = r.ReadCustomVersionContainer(
            ECustomVersionSerializationFormat.Guids,
            oldContainer,
            usmap,
        )
        assertEquals(3, out.size)
        assertEquals("FReleaseObjectVersion", CustomVersion.GetCustomVersionFriendlyNameFromGuid(out[0].Key))
        assertTrue(out[0].IsSerialized)
        assertEquals("FAnimObjectVersion", CustomVersion.GetCustomVersionFriendlyNameFromGuid(out[1].Key))
        assertFalse(out[1].IsSerialized)
        assertEquals("FAnimPhysObjectVersion", CustomVersion.GetCustomVersionFriendlyNameFromGuid(out[2].Key))
        assertFalse(out[2].IsSerialized)
    }

    @Test
    fun nameMapStringHashRollback() {
        val asset = UAsset()
        asset.ObjectVersion = ObjectVersion.VER_UE4_PROPERTY_GUID_IN_PROPERTY_TAG // 503 < 504
        asset.WillSerializeNameHashes = null

        // Write: an FString "Hello", then a uint "hash" value 3 (< 1024 -> triggers rollback)
        val w = UnrealBinaryWriter()
        w.Write(FString("Hello"))
        w.WriteUInt32(3)
        val r = AssetBinaryReader(w.toByteArray(), asset)

        val hashes = com.github.jpabscale.uasset4j.util.Out<Long>()
        val s = r.ReadNameMapString(hashes)
        assertEquals("Hello", s?.Value)
        assertEquals(0L, hashes.value)
        assertFalse(asset.WillSerializeNameHashes!!)
        // Position rolled back 4 bytes: the trailing uint is still unread
        assertEquals(w.position - 4, r.position)
        assertEquals(3L, r.ReadUInt32())
    }

    @Test
    fun fNameRoundTrip() {
        val asset = UAsset()
        val idx = asset.AddNameReference(FString("SampleName"))
        val w = AssetBinaryWriter(asset)
        w.Write(FName(asset, idx, 7))

        val r = AssetBinaryReader(w.toByteArray(), asset)
        val name = r.ReadFName()
        assertEquals("SampleName", name.Value?.Value)
        assertEquals(7, name.Number)
        assertEquals(idx, name.Index)
    }
}
