// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/DoublePropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class DoublePropertyData : PropertyData {
    var Value: Double
        get() = GetObject<Double>() ?: 0.0
        set(value) = SetObject(value)

    override val PropertyType: FString get() = CurrentPropertyType
    override val DefaultValue: Any get() = 0.0

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = reader.ReadDouble()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        writer.WriteDouble(Value)
        return 8
    }

    override fun toString(): String = Value.toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = 0.0
        d[0].toDoubleOrNull()?.let { Value = it }
    }

    override fun CreateClone(): PropertyData = DoublePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("DoubleProperty")
    }
}
