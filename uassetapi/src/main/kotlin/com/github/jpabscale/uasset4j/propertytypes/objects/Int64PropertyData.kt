// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/Int64PropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class Int64PropertyData : PropertyData {
    var Value: Long?
        get() = GetObject<Long>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = 0L

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = reader.ReadInt64()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        writer.WriteInt64(Value ?: 0)
        return 8
    }

    override fun toString(): String = (Value ?: 0).toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = 0
        d[0].toLongOrNull()?.let { Value = it }
    }

    override fun CreateClone(): PropertyData = Int64PropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("Int64Property")
    }
}
