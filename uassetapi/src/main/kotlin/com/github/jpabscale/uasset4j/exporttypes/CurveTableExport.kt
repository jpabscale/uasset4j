// Reimplemented from CUE4Parse (Apache-2.0) — Copyright (c) FabianFG and contributors
// Source: CUE4Parse/UE4/Assets/Exports/Engine/UCurveTable.cs
//
// uasset4j integration modeled on DataTableExport (MIT/UAssetAPI-ported): the tagged-property
// portion is handled by NormalExport.Read; the raw RowMap tail is read here.
//@parity:on EXC-002
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.curves.ECurveTableMode
import com.github.jpabscale.uasset4j.curves.UCurveTable
import com.github.jpabscale.uasset4j.customversions.FFortniteMainBranchObjectVersion
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.util.Out

/**
 * Export for a curve table. See [UCurveTable].
 *
 * The RowMap tail (mode byte + `FName`-keyed property-serialized curve structs) is read after the
 * tagged-property list. Row values are `SimpleCurve` (SimpleCurves mode) or `RichCurve`
 * (RichCurves mode), read via `StructPropertyData.Read` like CUE4Parse's `FStructFallback`.
 */
class CurveTableExport : NormalExport {
    override operator fun get(key: FName): PropertyData? {
        for (i in Data!!.indices) {
            if (Data!![i].Name == key) return Data!![i]
        }
        for (row in Table!!.RowMap) {
            if (row.key == key) return row.value
        }
        return null
    }

    override operator fun set(key: FName, value: PropertyData) {
        value.Name = key

        for (i in Data!!.indices) {
            if (Data!![i].Name == key) {
                Data!![i] = value
                return
            }
        }

        if (value is StructPropertyData) {
            val existing = Table!!.RowMap.keys.firstOrNull { it == key }
            if (existing != null) {
                Table!!.RowMap[existing] = value
                return
            }
            Table!!.RowMap[key] = value
        } else {
            Data!!.add(value)
        }
    }

    override operator fun get(key: String): PropertyData? {
        return this[FName.FromString(Asset!!, key)!!]
    }

    override operator fun set(key: String, value: PropertyData) {
        this[FName.FromString(Asset!!, key)!!] = value
    }

    var Table: UCurveTable? = null

    constructor(superExport: Export) : super(superExport)

    constructor(data: UCurveTable, asset: UAsset?, extras: ByteArray?) : super(asset, extras) {
        Table = data
    }

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)

        Table = UCurveTable()

        val numRows = reader.ReadInt32()

        val frmVer = reader.Asset?.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteMainBranchObjectVersion")) ?: -1
        val bUpgradingCurveTable = frmVer < FFortniteMainBranchObjectVersion.ShrinkCurveTableSize.ordinal
        Table!!.CurveTableMode = if (bUpgradingCurveTable) {
            if (numRows > 0) ECurveTableMode.RichCurves else ECurveTableMode.Empty
        } else {
            ECurveTableMode.entries[reader.ReadByte()]
        }

        val pcen2 = Out<FName?>()
        val pcen = reader.Asset!!.GetParentClassExportName(pcen2)
        for (i in 0 until numRows) {
            val rowName = reader.ReadFName()
            val rowStructType = when (Table!!.CurveTableMode) {
                ECurveTableMode.SimpleCurves -> "SimpleCurve"
                ECurveTableMode.RichCurves -> "RichCurve"
                else -> ""
            }
            val nextStruct = StructPropertyData(rowName)
            nextStruct.StructType = FName.DefineDummy(reader.Asset, rowStructType)
            nextStruct.Ancestry.Initialize(null, pcen, pcen2.value)
            nextStruct.Read(reader, false, 1)
            Table!!.RowMap[rowName] = nextStruct
        }
    }

    override fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        val ancestryNew = ancestrySoFar.clone()
        val pcen2 = Out<FName?>()
        val pcen = asset.GetParentClassExportName(pcen2)
        ancestryNew.SetAsParent(pcen, pcen2.value)

        if (Data != null) {
            for (i in Data!!.indices) Data!![i].ResolveAncestries(asset, ancestryNew)
        }
        if (Table?.RowMap != null) {
            for (row in Table!!.RowMap) row.value.ResolveAncestries(asset, ancestryNew)
        }
        super.ResolveAncestries(asset, ancestrySoFar)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        writer.WriteInt32(Table!!.RowMap.size)

        val frmVer = writer.Asset?.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFortniteMainBranchObjectVersion")) ?: -1
        val bUpgradingCurveTable = frmVer < FFortniteMainBranchObjectVersion.ShrinkCurveTableSize.ordinal
        if (!bUpgradingCurveTable) writer.WriteByte(Table!!.CurveTableMode.ordinal)

        for (row in Table!!.RowMap) {
            writer.Write(row.key)
            row.value.Write(writer, false)
        }
    }
}
//@parity:off EXC-002
