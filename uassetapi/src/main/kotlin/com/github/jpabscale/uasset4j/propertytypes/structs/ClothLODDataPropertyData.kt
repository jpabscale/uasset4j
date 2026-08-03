// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/ClothLODDataPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.structs.math.Vector4fPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class FMeshToMeshVertData {
    var PositionBaryCoordsAndDist: Vector4fPropertyData? = null
    var NormalBaryCoordsAndDist: Vector4fPropertyData? = null
    var TangentBaryCoordsAndDist: Vector4fPropertyData? = null
    var SourceMeshVertIndices: Array<Int>? = null
    var Weight: Float = 0.0f
    var Padding: Long = 0

    fun Read(reader: AssetBinaryReader) {
        PositionBaryCoordsAndDist = Vector4fPropertyData(FName.DefineDummy(reader.Asset, "PositionBaryCoordsAndDist"))
        PositionBaryCoordsAndDist!!.Offset = reader.position.toLong()
        PositionBaryCoordsAndDist!!.Read(reader, false, 0)

        NormalBaryCoordsAndDist = Vector4fPropertyData(FName.DefineDummy(reader.Asset, "NormalBaryCoordsAndDist"))
        NormalBaryCoordsAndDist!!.Offset = reader.position.toLong()
        NormalBaryCoordsAndDist!!.Read(reader, false, 0)

        TangentBaryCoordsAndDist = Vector4fPropertyData(FName.DefineDummy(reader.Asset, "TangentBaryCoordsAndDist"))
        TangentBaryCoordsAndDist!!.Offset = reader.position.toLong()
        TangentBaryCoordsAndDist!!.Read(reader, false, 0)

        SourceMeshVertIndices = Array(4) { reader.ReadUInt16() }

        Weight = reader.ReadSingle()

        Padding = reader.ReadUInt32()
    }

    fun Write(writer: AssetBinaryWriter): Int {
        var res = 0
        res += PositionBaryCoordsAndDist!!.Write(writer, false)
        res += NormalBaryCoordsAndDist!!.Write(writer, false)
        res += TangentBaryCoordsAndDist!!.Write(writer, false)

        for (i in 0 until 4) {
            writer.WriteUInt16(if (SourceMeshVertIndices!!.size > i) SourceMeshVertIndices!![i] else 0)
            res += 2
        }

        writer.WriteSingle(Weight); res += 4
        writer.WriteUInt32(Padding); res += 4

        return res
    }

    constructor()

    constructor(reader: AssetBinaryReader) {
        Read(reader)
    }

    constructor(
        positionBaryCoordsAndDist: Vector4fPropertyData,
        normalBaryCoordsAndDist: Vector4fPropertyData,
        tangentBaryCoordsAndDist: Vector4fPropertyData,
        sourceMeshVertIndices: Array<Int>,
        weight: Float,
        padding: Long,
    ) {
        PositionBaryCoordsAndDist = positionBaryCoordsAndDist
        NormalBaryCoordsAndDist = normalBaryCoordsAndDist
        TangentBaryCoordsAndDist = tangentBaryCoordsAndDist
        SourceMeshVertIndices = sourceMeshVertIndices
        Weight = weight
        Padding = padding
    }
}

open class ClothLODDataPropertyData : StructPropertyData {
    var TransitionUpSkinData: Array<FMeshToMeshVertData>? = null
    var TransitionDownSkinData: Array<FMeshToMeshVertData>? = null

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        StructType = FName.DefineDummy(reader.Asset, PropertyType)
        super.Read(reader, includeHeader, 1, leng2, PropertySerializationContext.StructFallback)

        val sizeUpData = reader.ReadInt32()
        TransitionUpSkinData = Array(sizeUpData) { FMeshToMeshVertData(reader) }

        val sizeDownData = reader.ReadInt32()
        TransitionDownSkinData = Array(sizeDownData) { FMeshToMeshVertData(reader) }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        StructType = FName.DefineDummy(writer.Asset, PropertyType)
        var res = super.Write(writer, includeHeader, PropertySerializationContext.StructFallback)

        if (TransitionUpSkinData == null) TransitionUpSkinData = emptyArray()
        writer.WriteInt32(TransitionUpSkinData!!.size); res += 4
        for (i in TransitionUpSkinData!!.indices) {
            res += TransitionUpSkinData!![i].Write(writer)
        }

        if (TransitionDownSkinData == null) TransitionDownSkinData = emptyArray()
        writer.WriteInt32(TransitionDownSkinData!!.size); res += 4
        for (i in TransitionDownSkinData!!.indices) {
            res += TransitionDownSkinData!![i].Write(writer)
        }

        return res
    }

    override fun toString(): String = super.toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        super.FromString(d, asset)
    }

    override fun CreateClone(): PropertyData = ClothLODDataPropertyData()

    constructor(name: FName?, forcedType: FName?) : super(name, forcedType)
    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("ClothLODData")
    }
}

class ClothLODDataCommonPropertyData : ClothLODDataPropertyData {
    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun CreateClone(): PropertyData = ClothLODDataCommonPropertyData()

    constructor(name: FName?, forcedType: FName?) : super(name, forcedType)
    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("ClothLODDataCommon")
    }
}
