// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/Int16PropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class Int16PropertyData : PropertyData {
    var Value: Short?
        get() = GetObject<Short>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = 0.toShort()

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = if (reader.Asset!!.HasUnversionedProperties && !serializationContext.IsNormal()) {
            reader.ReadInt64().toShort()
        } else {
            reader.ReadInt16()
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        if (writer.Asset!!.HasUnversionedProperties && !serializationContext.IsNormal()) {
            writer.WriteInt64((Value ?: 0).toLong())
            return 8
        }
        writer.WriteInt16(Value ?: 0)
        return 2
    }

    override fun toString(): String = (Value ?: 0).toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = 0
        d[0].toShortOrNull()?.let { Value = it }
    }

    override fun CreateClone(): PropertyData = Int16PropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("Int16Property")
    }
}
