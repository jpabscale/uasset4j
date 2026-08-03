// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/SplinePropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class SplinePropertyData : PropertyData {
    var CurrentImplementation: Int = 0

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        CurrentImplementation = reader.ReadByte()
        when (CurrentImplementation) {
            0 -> {}
            1 -> throw NotImplementedError("FLegacySpline serialization not implemented")
            else -> throw NotImplementedError("FNewSpline serialization not implemented")
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        val here = writer.position

        writer.WriteByte(CurrentImplementation)
        when (CurrentImplementation) {
            0 -> {}
            1 -> throw NotImplementedError("FLegacySpline serialization not implemented")
            else -> throw NotImplementedError("FNewSpline serialization not implemented")
        }

        return writer.position - here
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
    }

    override fun toString(): String = "(" + ")"

    override fun CreateClone(): PropertyData = SplinePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("Spline")
    }
}
