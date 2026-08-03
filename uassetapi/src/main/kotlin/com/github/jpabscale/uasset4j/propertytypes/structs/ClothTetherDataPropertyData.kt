// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/ClothTetherDataPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class ClothTetherDataPropertyData : StructPropertyData {
    var Tethers: Array<Array<Triple<Int, Int, Float>>>? = null

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        StructType = FName.DefineDummy(reader.Asset, CurrentPropertyType)
        super.Read(reader, false, 1, 0, PropertySerializationContext.StructFallback)

        val numElements = reader.ReadInt32()
        Tethers = Array(numElements) {
            val numInnerElements = reader.ReadInt32()
            Array(numInnerElements) {
                Triple(reader.ReadInt32(), reader.ReadInt32(), reader.ReadSingle())
            }
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        StructType = FName.DefineDummy(writer.Asset, CurrentPropertyType)
        var totalSize = super.Write(writer, includeHeader, PropertySerializationContext.StructFallback)

        if (Tethers == null) Tethers = emptyArray()
        writer.WriteInt32(Tethers!!.size)
        totalSize += 4
        for (i in Tethers!!.indices) {
            writer.WriteInt32(Tethers!![i].size)
            totalSize += 4
            for (j in Tethers!![i].indices) {
                writer.WriteInt32(Tethers!![i][j].first)
                writer.WriteInt32(Tethers!![i][j].second)
                writer.WriteSingle(Tethers!![i][j].third)
                totalSize += 4 * 2 + 4
            }
        }

        return totalSize
    }

    override fun CreateClone(): PropertyData = ClothTetherDataPropertyData()

    constructor(name: FName?, forcedType: FName?) : super(name, forcedType)
    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("ClothTetherData")
    }
}
