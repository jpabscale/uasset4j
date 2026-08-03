// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/Int8PropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class Int8PropertyData : PropertyData {
    var Value: Byte?
        get() = GetObject<Byte>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = 0.toByte()

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = reader.ReadByte().toByte()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        writer.WriteByte((Value ?: 0).toInt())
        return 1
    }

    override fun toString(): String = (Value ?: 0).toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = 0
        d[0].toByteOrNull()?.let { Value = it }
    }

    override fun CreateClone(): PropertyData = Int8PropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("Int8Property")
    }
}
