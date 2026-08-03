// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/SceneComponentExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FUE5SpecialProjectStreamObjectVersion
import com.github.jpabscale.uasset4j.propertytypes.objects.BoolPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unrealtypes.objects.core.math.FVector

class FBoxSphereBounds {
    var Origin: FVector = FVector()
    var BoxExtent: FVector = FVector()
    var SphereRadius: Double = 0.0
}

class SceneComponentExport : ActorComponentExport {
    var bComputeBoundsOnceForGame: Boolean = false
    var bComputedBoundsOnceForGame: Boolean = false
    var bIsCooked: Boolean = false
    var Bounds: FBoxSphereBounds = FBoxSphereBounds()

    constructor(superExport: Export) : super(superExport)

    constructor(asset: UAsset?, extras: ByteArray?) : super(asset, extras)

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)
        val bComputeBounds = ((this[FName.DefineDummy(reader.Asset, "bComputeBoundsOnceForGame")] as? BoolPropertyData)?.Value ?: false) ||
            ((this[FName.DefineDummy(reader.Asset, "bComputedBoundsOnceForGame")] as? BoolPropertyData)?.Value ?: false)
        if (bComputeBounds && reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FUE5SpecialProjectStreamObjectVersion")) >= FUE5SpecialProjectStreamObjectVersion.SerializeSceneComponentStaticBounds.ordinal) {
            bIsCooked = reader.ReadBooleanInt()
            if (bIsCooked) {
                val Origin = FVector(reader)
                val BoxExtent = FVector(reader)
                val SphereRadius = if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) reader.ReadDouble() else reader.ReadSingle().toDouble()
                Bounds = FBoxSphereBounds().apply {
                    this.Origin = Origin
                    this.BoxExtent = BoxExtent
                    this.SphereRadius = SphereRadius
                }
            }
        }
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        val bComputeBounds = ((this[FName.DefineDummy(writer.Asset, "bComputeBoundsOnceForGame")] as? BoolPropertyData)?.Value ?: false) ||
            ((this[FName.DefineDummy(writer.Asset, "bComputedBoundsOnceForGame")] as? BoolPropertyData)?.Value ?: false)
        if (bComputeBounds && writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FUE5SpecialProjectStreamObjectVersion")) >= FUE5SpecialProjectStreamObjectVersion.SerializeSceneComponentStaticBounds.ordinal) {
            writer.WriteBooleanInt(bIsCooked)
            if (bIsCooked) {
                Bounds.Origin.Write(writer)
                Bounds.BoxExtent.Write(writer)
                if (writer.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.LARGE_WORLD_COORDINATES) {
                    writer.WriteDouble(Bounds.SphereRadius)
                } else {
                    writer.WriteSingle(Bounds.SphereRadius.toFloat())
                }
            }
        }
    }
}
