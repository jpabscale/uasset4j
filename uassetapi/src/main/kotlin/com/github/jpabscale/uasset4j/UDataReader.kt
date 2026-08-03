// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/AssetBinaryReader.cs
package com.github.jpabscale.uasset4j

import com.github.jpabscale.uasset4j.customversions.FReleaseObjectVersion
import com.github.jpabscale.uasset4j.kismet.bytecode.EExprToken
import com.github.jpabscale.uasset4j.kismet.bytecode.ExpressionSerializer
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetExpression
import com.github.jpabscale.uasset4j.kismet.bytecode.KismetPropertyPointer
import com.github.jpabscale.uasset4j.propertytypes.objects.FFieldPath
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FLocMetadataObject
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FObjectThumbnail
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unversioned.Usmap
import com.github.jpabscale.uasset4j.util.Out
import java.io.EOFException

enum class ECustomVersionSerializationFormat {
    Unknown,
    Guids,
    Enums,
    Optimized,
}

/** Any binary reader used in the parsing of Unreal file types. */
open class UnrealBinaryReader(
    private val data: ByteArray,
) {
    var position: Int = 0
        set(value) {
            if (value < 0) throw IndexOutOfBoundsException("negative position $value")
            field = value
        }
    val length: Int get() = data.size

    protected fun require(n: Int) {
        if (position + n > data.size) {
            throw EOFException("Unexpected end of stream at $position (need $n bytes)")
        }
    }

    fun ReadByte(): Int {
        require(1)
        return data[position++].toInt() and 0xFF
    }

    fun ReadSByte(): Int {
        require(1)
        return data[position++].toInt()
    }

    fun ReadBytes(n: Int): ByteArray {
        if (n < 0) throw IndexOutOfBoundsException("n cannot be negative")
        if (n == 0) return ByteArray(0)
        require(n)
        val out = data.copyOfRange(position, position + n)
        position += n
        return out
    }

    fun ReadInt16(): Short {
        require(2)
        val v = (data[position].toInt() and 0xFF) or ((data[position + 1].toInt() and 0xFF) shl 8)
        position += 2
        return v.toShort()
    }

    fun ReadUInt16(): Int {
        require(2)
        val v = (data[position].toInt() and 0xFF) or ((data[position + 1].toInt() and 0xFF) shl 8)
        position += 2
        return v
    }

    fun ReadInt32(): Int {
        require(4)
        val v = (data[position].toInt() and 0xFF) or
            ((data[position + 1].toInt() and 0xFF) shl 8) or
            ((data[position + 2].toInt() and 0xFF) shl 16) or
            (data[position + 3].toInt() shl 24)
        position += 4
        return v
    }

    fun ReadUInt32(): Long {
        return ReadInt32().toLong() and 0xFFFFFFFFL
    }

    fun ReadInt64(): Long {
        require(8)
        var v = 0L
        for (i in 0 until 8) v = v or ((data[position + i].toLong() and 0xFFL) shl (8 * i))
        position += 8
        return v
    }

    fun ReadUInt64(): Long = ReadInt64()

    fun ReadSingle(): Float {
        return Float.fromBits(ReadInt32())
    }

    fun ReadDouble(): Double {
        return Double.fromBits(ReadInt64())
    }

    fun ReadBooleanInt(): Boolean {
        val i = ReadInt32()
        return when (i) {
            1 -> true
            0 -> false
            else -> throw FormatException("Invalid boolean value $i")
        }
    }

    fun ReadBooleanByte(): Boolean {
        val i = ReadByte()
        return when (i) {
            1 -> true
            0 -> false
            else -> throw FormatException("Invalid boolean value $i")
        }
    }

    fun ReadGuid(): FGuid = FGuid.fromBytes(ReadBytes(16))

    fun ReadString(): String? = ReadFString()?.Value

    open fun ReadFString(): FString? {
        val length = ReadInt32()
        if (length > MainSerializer.MaxSerializedArrayLength) {
            throw IllegalStateException("Invalid FString length: $length")
        }
        return when {
            length < 0 -> {
                val len = -length * 2
                val data = ReadBytes(len)
                FString(String(data, 0, len - 2, Charsets.UTF_16LE), Charsets.UTF_16LE)
            }
            length > 0 -> {
                val data = ReadBytes(length)
                FString(String(data, 0, length - 1, Charsets.UTF_8), Charsets.UTF_8)
            }
            else -> null
        }
    }

    open fun ReadUtf8String(): FString? {
        val length = ReadInt32()
        if (length > MainSerializer.MaxSerializedArrayLength) {
            throw IllegalStateException("Invalid UTF-8 string length: $length")
        }
        return when {
            length < 0 -> throw FormatException("Invalid UTF-8 string length: $length")
            length > 0 -> {
                val data = ReadBytes(length)
                FString(String(data, 0, length, Charsets.UTF_8), Charsets.UTF_8)
            }
            else -> null
        }
    }

    open fun ReadNameMapString(hashes: Out<Long>): FString? {
        hashes.value = 0
        val str = ReadFString()
        val abr = this as? AssetBinaryReader
        if (abr != null) {
            val ua = abr.Asset
            if (ua != null && ua.WillSerializeNameHashes != false && !str?.Value.isNullOrEmpty()) {
                val hashVal = ReadUInt32()
                hashes.value = hashVal
                if (hashVal < (1 shl 10) && ua.ObjectVersion < ObjectVersion.VER_UE4_NAME_HASHES_SERIALIZED) {
                    // "i lied, there's actually no hashes"
                    ua.WillSerializeNameHashes = false
                    hashes.value = 0
                    position -= 4
                } else {
                    ua.WillSerializeNameHashes = true
                }
            }
        }
        return str
    }

    fun ReadCustomVersionContainer(
        format: ECustomVersionSerializationFormat,
        oldCustomVersionContainer: List<CustomVersion>? = null,
        mappings: Usmap? = null,
    ): MutableList<CustomVersion> {
        val newCustomVersionContainer = mutableListOf<CustomVersion>()
        val existingCustomVersions = mutableSetOf<FGuid>()
        when (format) {
            ECustomVersionSerializationFormat.Enums ->
                throw NotImplementedError("Custom version serialization format Enums is currently unimplemented")
            ECustomVersionSerializationFormat.Guids -> {
                val numCustomVersions = ReadInt32()
                for (i in 0 until numCustomVersions) {
                    val customVersionID = ReadGuid()
                    val customVersionNumber = ReadInt32()
                    newCustomVersionContainer.add(
                        CustomVersion(customVersionID, customVersionNumber).apply { Name = ReadFString() }
                    )
                    existingCustomVersions.add(customVersionID)
                }
            }
            ECustomVersionSerializationFormat.Optimized -> {
                val numCustomVersions = ReadInt32()
                for (i in 0 until numCustomVersions) {
                    val customVersionID = ReadGuid()
                    val customVersionNumber = ReadInt32()
                    newCustomVersionContainer.add(CustomVersion(customVersionID, customVersionNumber))
                    existingCustomVersions.add(customVersionID)
                }
            }
            ECustomVersionSerializationFormat.Unknown -> {}
        }

        val mappingsContainer = mappings?.CustomVersionContainer
        if (mappingsContainer != null && mappingsContainer.size > 0) {
            for (entry in mappingsContainer) {
                if (!existingCustomVersions.contains(entry.Key)) newCustomVersionContainer.add(entry.SetIsSerialized(false))
            }
        }

        if (oldCustomVersionContainer != null) {
            for (entry in oldCustomVersionContainer) {
                if (!existingCustomVersions.contains(entry.Key)) newCustomVersionContainer.add(entry.SetIsSerialized(false))
            }
        }

        return newCustomVersionContainer
    }
}

/** Reads primitive data types from Unreal Engine assets. */
class AssetBinaryReader(
    data: ByteArray,
    asset: UAsset? = null,
    loadUexp: Boolean = true,
) : UnrealBinaryReader(data) {
    var Asset: UAsset? = asset
    var LoadUexp: Boolean = loadUexp

    fun ReadPropertyGuid(): FGuid? {
        val a = Asset
        if (a?.HasUnversionedProperties == true) return null
        if ((a?.ObjectVersion ?: ObjectVersion.UNKNOWN) >= ObjectVersion.VER_UE4_PROPERTY_GUID_IN_PROPERTY_TAG) {
            val hasPropertyGuid = ReadBooleanByte()
            if (hasPropertyGuid) return ReadGuid()
        }
        return null
    }

    fun ReadFName(): FName {
        val nameMapPointer = ReadInt32()
        val number = ReadInt32()
        return FName(Asset, nameMapPointer, number)
    }

    inline fun <reified T> ReadArray(length: Int, crossinline readElement: () -> T): Array<T> {
        if (length == 0) return emptyArray()
        if (length > MainSerializer.MaxSerializedArrayLength) throw FormatException("Invalid array length: $length")
        return Array(length) { readElement() }
    }

    inline fun <reified T> ReadArray(crossinline readElement: () -> T): Array<T> {
        val arrayLength = ReadInt32()
        return ReadArray(arrayLength, readElement)
    }

    inline fun <reified K, reified V> ReadMap(length: Int, crossinline keyGetter: () -> K, crossinline valueGetter: () -> V): LinkedHashMap<K, V> {
        val map = LinkedHashMap<K, V>()
        val arr = ReadArray(length) { keyGetter() to valueGetter() }
        for ((k, v) in arr) map[k] = v
        return map
    }

    inline fun <reified K, reified V> ReadMap(crossinline keyGetter: () -> K, crossinline valueGetter: () -> V): LinkedHashMap<K, V> {
        val length = ReadInt32()
        return ReadMap(length, keyGetter, valueGetter)
    }

    fun ReadObjectThumbnail(): FObjectThumbnail {
        val thumb = FObjectThumbnail()

        thumb.Width = ReadInt32()
        thumb.Height = ReadInt32()
        val imageBytesCount = ReadInt32()
        thumb.CompressedImageData = if (imageBytesCount > 0) ReadBytes(imageBytesCount) else ByteArray(0)

        return thumb
    }

    fun ReadLocMetadataObject(): FLocMetadataObject {
        val locMetadataObject = FLocMetadataObject()

        val valueCount = ReadInt32()
        if (valueCount > 0) throw NotImplementedError("TODO: implement ReadLocMetadataObject")

        return locMetadataObject
    }

    // !!!!!
    // THE FOLLOWING METHODS ARE INTENDED ONLY TO BE USED IN PARSING KISMET BYTECODE.
    // !!!!!

    fun XFERSTRING(): String {
        val readData = mutableListOf<Byte>()
        while (true) {
            val newVal = ReadByte()
            if (newVal == 0) break
            readData.add(newVal.toByte())
        }
        return String(readData.toByteArray(), Charsets.UTF_8)
    }

    fun XFERUNICODESTRING(): String {
        val readData = mutableListOf<Byte>()
        while (true) {
            val newVal1 = ReadByte()
            val newVal2 = ReadByte()
            if (newVal1 == 0 && newVal2 == 0) break
            readData.add(newVal1.toByte())
            readData.add(newVal2.toByte())
        }
        return String(readData.toByteArray(), Charsets.UTF_16LE)
    }

    fun XFERTEXT() {
    }

    fun XFERNAME(): FName = ReadFName()

    fun XFER_FUNC_NAME(): FName = XFERNAME()

    fun XFERPTR(): FPackageIndex = FPackageIndex(ReadInt32())

    fun XFER_FUNC_POINTER(): FPackageIndex = XFERPTR()

    fun XFER_PROP_POINTER(): KismetPropertyPointer {
        if (Asset!!.GetCustomVersion<FReleaseObjectVersion>() >= FReleaseObjectVersion.FFieldPathOwnerSerialization.ordinal) {
            val numEntries = ReadInt32()
            val allNames = Array(numEntries) { ReadFName() }
            val owner = XFER_OBJECT_POINTER()
            return KismetPropertyPointer(FFieldPath(allNames, owner, Asset!!.Exports.size))
        } else {
            return KismetPropertyPointer(XFERPTR())
        }
    }

    fun XFER_OBJECT_POINTER(): FPackageIndex = XFERPTR()

    fun ReadExpressionArray(endToken: EExprToken): Array<KismetExpression> {
        val newData = mutableListOf<KismetExpression>()
        var currExpression: KismetExpression? = null
        while (currExpression == null || currExpression.Token != endToken) {
            if (currExpression != null) newData.add(currExpression)
            currExpression = ExpressionSerializer.ReadExpression(this)
        }
        return newData.toTypedArray()
    }
}
