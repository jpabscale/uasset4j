// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/UInt16PropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class UInt16PropertyData : PropertyData {
    var Value: Int?
        get() = GetObject<Int>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = 0

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = if (reader.Asset!!.HasUnversionedProperties && !serializationContext.IsNormal() && serializationContext != PropertySerializationContext.Array) {
            reader.ReadInt64().toInt()
        } else {
            reader.ReadUInt16()
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        if (writer.Asset!!.HasUnversionedProperties && !serializationContext.IsNormal() && serializationContext != PropertySerializationContext.Array) {
            writer.WriteInt64((Value ?: 0).toLong())
            return 8
        }
        writer.WriteUInt16(Value ?: 0)
        return 2
    }

    override fun toString(): String = (Value ?: 0).toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = 0
        d[0].toUShortOrNull()?.let { Value = it.toInt() }
    }

    override fun CreateClone(): PropertyData = UInt16PropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("UInt16Property")
    }
}
