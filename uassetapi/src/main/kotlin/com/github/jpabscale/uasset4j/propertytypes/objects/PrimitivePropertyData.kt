// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/*.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5

class IntPropertyData : PropertyData {
    var Value: Int?
        get() = GetObject<Int>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = 0

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = reader.ReadInt32()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        writer.WriteInt32(Value ?: 0)
        return 4
    }

    override fun toString(): String = (Value ?: 0).toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = 0
        d[0].toIntOrNull()?.let { Value = it }
    }

    override fun CreateClone(): PropertyData = IntPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("IntProperty")
    }
}

class FloatPropertyData : PropertyData {
    var Value: Float
        get() = GetObject<Float>() ?: 0f
        set(value) = SetObject(value)

    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = 0f

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = reader.ReadSingle()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        writer.WriteSingle(Value)
        return 4
    }

    override fun toString(): String = Value.toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = 0f
        d[0].toFloatOrNull()?.let { Value = it }
    }

    override fun CreateClone(): PropertyData = FloatPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("FloatProperty")
    }
}

class BoolPropertyData : PropertyData {
    var Value: Boolean?
        get() = GetObject<Boolean>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = false

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (reader.Asset!!.HasUnversionedProperties || reader.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
            Value = reader.ReadBooleanByte()
        } else {
            if (serializationContext == PropertySerializationContext.Map || serializationContext == PropertySerializationContext.Array) {
                Value = reader.ReadBooleanByte()
            } else {
                Value = PropertyTagFlags.HasFlag(EPropertyTagFlags.BoolTrue)
            }
        }
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (writer.Asset!!.HasUnversionedProperties || writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
            writer.WriteBooleanByte(Value ?: false)
        } else if (serializationContext == PropertySerializationContext.Map || serializationContext == PropertySerializationContext.Array) {
            writer.WriteBooleanByte(Value ?: false)
        }

        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        return 0
    }

    override fun toString(): String = (Value ?: false).toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = d[0] == "1" || d[0].lowercase() == "true"
    }

    override fun CreateClone(): PropertyData = BoolPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("BoolProperty")
    }
}

class NamePropertyData : PropertyData {
    var Value: FName?
        get() = GetObject<FName>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = reader.ReadFName()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        writer.Write(Value)
        return 8
    }

    override fun CanBeZero(asset: UAsset): Boolean {
        return Value?.Value?.Value == null
    }

    override fun toString(): String = Value?.toString() ?: "null"

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = FName.FromString(asset, d[0])
    }

    override fun CreateClone(): PropertyData = NamePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("NameProperty")
    }
}

class StrPropertyData : PropertyData {
    var Value: FString?
        get() = GetObject<FString>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = reader.ReadFString()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        val here = writer.position
        writer.Write(Value)
        return writer.position - here
    }

    override fun toString(): String = Value.toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        var encoding = Charsets.UTF_8
        if (d.size >= 5 && d[4] == "utf-16") encoding = Charsets.UTF_16LE
        Value = FString.FromString(d[0], encoding)
    }

    override fun CreateClone(): PropertyData = StrPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("StrProperty")
    }
}
