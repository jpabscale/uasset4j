// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/AssetBinaryWriter.cs
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.customversions.FReleaseObjectVersion
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FLocMetadataObject
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FObjectThumbnail
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion

/**
 * Pass-through buffer for detecting non-zero byte writes for CanBeZero.
 * C# wraps a Stream; here it wraps the writer's buffer and tracks [HasWrittenNonZero].
 */
class CanBeZeroBuffer {
    var HasWrittenNonZero = false
        private set

    fun onWrite(bytes: ByteArray, offset: Int = 0, count: Int = bytes.size) {
        if (!HasWrittenNonZero) {
            for (i in offset until offset + count) {
                if (bytes[i] != 0.toByte()) {
                    HasWrittenNonZero = true
                    break
                }
            }
        }
    }
}

/** Any binary writer used in the parsing of Unreal file types. */
open class UnrealBinaryWriter {
    private var data = ByteArray(4096)
    var position: Int = 0
        set(value) {
            if (value < 0) throw IndexOutOfBoundsException("negative position $value")
            field = value
        }

    fun toByteArray(): ByteArray = data.copyOf(position)

    private fun ensure(n: Int) {
        if (position + n > data.size) {
            data = data.copyOf(maxOf(data.size * 2, position + n))
        }
    }

    fun WriteByte(value: Int) {
        ensure(1)
        data[position++] = value.toByte()
    }

    fun WriteSByte(value: Int) = WriteByte(value and 0xFF)

    fun WriteBytes(bytes: ByteArray) {
        ensure(bytes.size)
        bytes.copyInto(data, position)
        position += bytes.size
    }

    fun WriteInt16(value: Short) {
        ensure(2)
        data[position] = (value.toInt() and 0xFF).toByte()
        data[position + 1] = ((value.toInt() ushr 8) and 0xFF).toByte()
        position += 2
    }

    fun WriteUInt16(value: Int) {
        ensure(2)
        data[position] = (value and 0xFF).toByte()
        data[position + 1] = ((value ushr 8) and 0xFF).toByte()
        position += 2
    }

    fun WriteInt32(value: Int) {
        ensure(4)
        data[position] = (value and 0xFF).toByte()
        data[position + 1] = ((value ushr 8) and 0xFF).toByte()
        data[position + 2] = ((value ushr 16) and 0xFF).toByte()
        data[position + 3] = ((value ushr 24) and 0xFF).toByte()
        position += 4
    }

    fun WriteUInt32(value: Long) {
        WriteInt32(value.toInt())
    }

    fun WriteInt64(value: Long) {
        ensure(8)
        for (i in 0 until 8) data[position + i] = ((value ushr (8 * i)) and 0xFF).toByte()
        position += 8
    }

    fun WriteUInt64(value: Long) = WriteInt64(value)

    fun WriteSingle(value: Float) = WriteInt32(value.toRawBits())

    fun WriteDouble(value: Double) = WriteInt64(value.toRawBits())

    fun WriteBooleanByte(value: Boolean) = WriteByte(if (value) 1 else 0)

    fun WriteBooleanInt(value: Boolean) = WriteInt32(if (value) 1 else 0)

    fun WriteGuid(value: FGuid) = WriteBytes(value.toByteArray())

    /** Overwrites a 32-bit little-endian value at [offset] without moving [position]. */
    fun WriteInt32At(offset: Int, value: Int) {
        data[offset] = (value and 0xFF).toByte()
        data[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        data[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        data[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    open fun WriteString(value: String?) = Write(FString(value))

    open fun Write(value: FString?): Int {
        val raw = value?.Value
        return when {
            raw == null -> {
                WriteInt32(0)
                4
            }
            else -> {
                val nullTerminatedStr = raw + "\u0000"
                val isUnicode = value.Encoding == Charsets.UTF_16LE
                WriteInt32(if (isUnicode) -nullTerminatedStr.length else nullTerminatedStr.length)
                val actualStrData = if (isUnicode) {
                    nullTerminatedStr.toByteArray(Charsets.UTF_16LE)
                } else {
                    nullTerminatedStr.toByteArray(Charsets.UTF_8)
                }
                WriteBytes(actualStrData)
                actualStrData.size + 4
            }
        }
    }

    fun WriteUtf8String(value: FString?): Int {
        val raw = value?.Value
        return when {
            raw == null -> {
                WriteInt32(0)
                4
            }
            else -> {
                WriteInt32(raw.length)
                val actualStrData = raw.toByteArray(Charsets.UTF_8)
                WriteBytes(actualStrData)
                actualStrData.size + 4
            }
        }
    }

    fun WriteCustomVersionContainer(
        format: ECustomVersionSerializationFormat,
        customVersionContainer: List<CustomVersion>?,
    ) {
        val num = customVersionContainer?.size ?: 0

        when (format) {
            ECustomVersionSerializationFormat.Enums ->
                throw NotImplementedError("Custom version serialization format Enums is currently unimplemented")
            ECustomVersionSerializationFormat.Guids -> {
                val numLoc = position
                WriteInt32(0)

                var realNum = 0
                for (i in 0 until num) {
                    val entry = customVersionContainer!![i]
                    if (entry.Version <= 0 || !entry.IsSerialized) continue
                    if (entry.Key == CustomVersion.UnusedCustomVersionKey) continue
                    realNum++
                    WriteGuid(entry.Key)
                    WriteInt32(entry.Version)
                    Write(entry.Name)
                }

                val endLoc = position
                position = numLoc
                WriteInt32(realNum)
                position = endLoc
            }
            ECustomVersionSerializationFormat.Optimized -> {
                val numLoc = position
                WriteInt32(0)

                var realNum = 0
                for (i in 0 until num) {
                    val entry = customVersionContainer!![i]
                    if (entry.Version < 0 || !entry.IsSerialized) continue
                    if (entry.Key == CustomVersion.UnusedCustomVersionKey) continue
                    realNum++
                    WriteGuid(entry.Key)
                    WriteInt32(entry.Version)
                }

                val endLoc = position
                position = numLoc
                WriteInt32(realNum)
                position = endLoc
            }
            ECustomVersionSerializationFormat.Unknown -> {}
        }
    }
}

/** Writes primitive data types from Unreal Engine assets. */
class AssetBinaryWriter(
    asset: UAsset? = null,
) : UnrealBinaryWriter() {
    var Asset: UAsset? = asset

    fun Write(name: FName?) {
        var n = name
        if (n == null) n = FName(Asset, 0, 0)
        WriteInt32(n.Index)
        WriteInt32(n.Number)
    }

    fun WritePropertyGuid(guid: FGuid?) {
        val a = Asset
        if (a?.HasUnversionedProperties == true) return
        if ((a?.ObjectVersion ?: ObjectVersion.UNKNOWN) >= ObjectVersion.VER_UE4_PROPERTY_GUID_IN_PROPERTY_TAG) {
            WriteBooleanByte(guid != null)
            if (guid != null) WriteGuid(guid)
        }
    }

    fun Write(thumbnail: FObjectThumbnail) {
        WriteInt32(thumbnail.Width)
        WriteInt32(thumbnail.Height)
        WriteInt32(thumbnail.CompressedImageData.size)
        if (thumbnail.CompressedImageData.size > 0) WriteBytes(thumbnail.CompressedImageData)
    }

    fun Write(metadataObject: FLocMetadataObject) {
        WriteInt32(metadataObject.Values.size)
        if (metadataObject.Values.size > 0) throw NotImplementedError("TODO: implement Write(FLocMetadataObject)")
    }

    // !!!!!
    // THE FOLLOWING METHODS ARE INTENDED ONLY TO BE USED IN PARSING KISMET BYTECODE.
    // !!!!!

    fun XFERSTRING(val_: String): Int {
        val start = position
        WriteBytes((val_ + "\u0000").toByteArray(Charsets.UTF_8))
        return position - start
    }

    fun XFERUNICODESTRING(val_: String): Int {
        val start = position
        WriteBytes((val_ + "\u0000").toByteArray(Charsets.UTF_16LE))
        return position - start
    }

    fun XFERNAME(val_: FName): Int {
        Write(val_)
        return 12 // FScriptName's iCode offset is 12 bytes, not 8
    }

    fun XFER_FUNC_NAME(val_: FName): Int = XFERNAME(val_)

    private val pointerSize = 8

    fun XFERPTR(val_: FPackageIndex?): Int {
        WriteInt32(val_?.Index ?: 0)
        return pointerSize // iCode offset uses the in-memory pointer size, not the on-disk FPackageIndex size
    }

    fun XFER_FUNC_POINTER(val_: FPackageIndex?): Int = XFERPTR(val_)

    fun XFER_PROP_POINTER(val_: KismetPropertyPointer): Int {
        if (Asset!!.GetCustomVersion<FReleaseObjectVersion>() >= FReleaseObjectVersion.FFieldPathOwnerSerialization.ordinal) {
            WriteInt32(val_.New!!.Path.size)
            for (i in val_.New!!.Path.indices) {
                XFERNAME(val_.New!!.Path[i])
            }
            XFER_OBJECT_POINTER(val_.New!!.ResolvedOwner)
        } else {
            XFERPTR(val_.Old)
        }
        return pointerSize
    }

    fun XFER_OBJECT_POINTER(val_: FPackageIndex?): Int = XFERPTR(val_)
}
