// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/PerPlatformProperties.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.objects.engine.FFrameRate
import com.github.jpabscale.uasset4j.util.Out

abstract class TPerPlatformPropertyData<T> : PropertyData {
    var Value: Array<T>?
        get() = GetObject()
        set(v) = SetObject(v)

    constructor(name: FName?) : super(name) {
        Value = emptyTypedArray()
    }

    constructor() : super() {
        Value = emptyTypedArray()
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T> emptyTypedArray(): Array<T> = emptyArray<Any?>() as Array<T>

private fun TryParseBool(s: String): Boolean? = when (s.trim().lowercase()) {
    "true" -> true
    "false" -> false
    else -> null
}

class PerPlatformBoolPropertyData : TPerPlatformPropertyData<Boolean> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        val numEntries = reader.ReadInt32()
        Value = Array(numEntries) { reader.ReadInt32() == 1 }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        writer.WriteInt32(Value!!.size)
        for (i in Value!!.indices) {
            writer.WriteBooleanInt(Value!![i])
        }
        return 4 + 4 * Value!!.size
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        val valueList = mutableListOf<Boolean>()
        TryParseBool(d[0])?.let { valueList.add(it) }
        TryParseBool(d[1])?.let { valueList.add(it) }
        TryParseBool(d[2])?.let { valueList.add(it) }
        TryParseBool(d[3])?.let { valueList.add(it) }
        Value = valueList.toTypedArray()
    }

    override fun toString(): String {
        var oup = "("
        for (i in Value!!.indices) {
            oup += Value!![i].toString() + ", "
        }
        return oup.dropLast(2) + ")"
    }

    override fun CreateClone(): PropertyData = PerPlatformBoolPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("PerPlatformBool")
    }
}

class PerPlatformFloatPropertyData : TPerPlatformPropertyData<Float> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        val numEntries = reader.ReadInt32()
        Value = Array(numEntries) { reader.ReadSingle() }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        writer.WriteInt32(Value!!.size)
        for (i in Value!!.indices) {
            writer.WriteSingle(Value!![i])
        }
        return 4 + 4 * Value!!.size
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        val valueList = mutableListOf<Float>()
        d[0].toFloatOrNull()?.let { valueList.add(it) }
        d[1].toFloatOrNull()?.let { valueList.add(it) }
        d[2].toFloatOrNull()?.let { valueList.add(it) }
        d[3].toFloatOrNull()?.let { valueList.add(it) }
        Value = valueList.toTypedArray()
    }

    override fun toString(): String {
        var oup = "("
        for (i in Value!!.indices) {
            oup += Value!![i].toString() + ", "
        }
        return oup.dropLast(2) + ")"
    }

    override fun CreateClone(): PropertyData = PerPlatformFloatPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("PerPlatformFloat")
    }
}

class PerPlatformIntPropertyData : TPerPlatformPropertyData<Int> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        val numEntries = reader.ReadInt32()
        Value = Array(numEntries) { reader.ReadInt32() }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        writer.WriteInt32(Value!!.size)
        for (i in Value!!.indices) {
            writer.WriteInt32(Value!![i])
        }
        return 4 + 4 * Value!!.size
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        val valueList = mutableListOf<Int>()
        d[0].toIntOrNull()?.let { valueList.add(it) }
        d[1].toIntOrNull()?.let { valueList.add(it) }
        d[2].toIntOrNull()?.let { valueList.add(it) }
        d[3].toIntOrNull()?.let { valueList.add(it) }
        Value = valueList.toTypedArray()
    }

    override fun toString(): String {
        var oup = "("
        for (i in Value!!.indices) {
            oup += Value!![i].toString() + ", "
        }
        return oup.dropLast(2) + ")"
    }

    override fun CreateClone(): PropertyData = PerPlatformIntPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("PerPlatformInt")
    }
}

class PerPlatformFrameRatePropertyData : TPerPlatformPropertyData<FFrameRate> {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        val numEntries = reader.ReadInt32()
        Value = Array(numEntries) { FFrameRate(reader.ReadInt32(), reader.ReadInt32()) }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        writer.WriteInt32(Value!!.size)
        for (i in Value!!.indices) {
            writer.WriteInt32(Value!![i].Numerator)
            writer.WriteInt32(Value!![i].Denominator)
        }
        return 4 + 4 * 2 * Value!!.size
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        val valueList = mutableListOf<FFrameRate>()
        val res1 = Out<FFrameRate>()
        if (FFrameRate.TryParse(d[0], res1)) valueList.add(res1.value!!)
        val res2 = Out<FFrameRate>()
        if (FFrameRate.TryParse(d[1], res2)) valueList.add(res2.value!!)
        val res3 = Out<FFrameRate>()
        if (FFrameRate.TryParse(d[2], res3)) valueList.add(res3.value!!)
        val res4 = Out<FFrameRate>()
        if (FFrameRate.TryParse(d[3], res4)) valueList.add(res4.value!!)
        Value = valueList.toTypedArray()
    }

    override fun toString(): String {
        var oup = "("
        for (i in Value!!.indices) {
            oup += Value!![i].toString() + ", "
        }
        return oup.dropLast(2) + ")"
    }

    override fun CreateClone(): PropertyData = PerPlatformFrameRatePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("PerPlatformFrameRate")
    }
}
