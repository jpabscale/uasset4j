// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Ranges/FloatRangePropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.ranges

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FloatRangePropertyData : PropertyData {
    var LowerBound: Float = 0f
    var UpperBound: Float = 0f

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        LowerBound = reader.ReadSingle()
        UpperBound = reader.ReadSingle()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        writer.WriteSingle(LowerBound)
        writer.WriteSingle(UpperBound)
        return 8
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        d[0].toFloatOrNull()?.let { LowerBound = it }
        d[1].toFloatOrNull()?.let { UpperBound = it }
    }

    override fun toString(): String = "($LowerBound, $UpperBound)"

    override fun CreateClone(): PropertyData = FloatRangePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("FloatRange")
    }
}
