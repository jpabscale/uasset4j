// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/DataTableExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.propertytypes.objects.ObjectPropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.util.Out

/**
 * Imported spreadsheet table.
 */
class UDataTable {
    var Data: MutableList<StructPropertyData> = mutableListOf()

    constructor()

    constructor(data: MutableList<StructPropertyData>) {
        Data = data
    }
}

/**
 * Export for an imported spreadsheet table. See [UDataTable].
 */
class DataTableExport : NormalExport {
    override operator fun get(key: FName): PropertyData? {
        for (i in Data!!.indices) {
            if (Data!![i].Name == key) return Data!![i]
        }
        for (i in Table!!.Data.indices) {
            if (Table!!.Data[i].Name == key) return Table!!.Data[i]
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
            for (i in Table!!.Data.indices) {
                if (Table!!.Data[i].Name == key) {
                    Table!!.Data[i] = value
                    return
                }
            }

            Table!!.Data.add(value)
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

    var Table: UDataTable? = null

    constructor(superExport: Export) : super(superExport)

    constructor(data: UDataTable, asset: UAsset?, extras: ByteArray?) : super(asset, extras) {
        Table = data
    }

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)

        // Find an ObjectProperty named RowStruct
        var decidedStructType = FName.DefineDummy(reader.Asset, "Generic")
        for (thisData in Data!!) {
            if (thisData.Name!!.Value!!.Value == "RowStruct" && thisData is ObjectPropertyData && thisData.Value!!.IsImport()) {
                decidedStructType = thisData.ToImport(reader.Asset!!)!!.ObjectName!!
                break
            }
        }

        if (decidedStructType.toString() == "Generic") {
            // overrides here...
            val exportClassTypeName = this.GetExportClassType()
            val exportClassType = exportClassTypeName!!.Value!!.Value
            when (exportClassType) {
                "CommonGenericInputActionDataTable" -> decidedStructType = FName.DefineDummy(reader.Asset, "CommonInputActionDataBase")
            }
        }

        Table = UDataTable()

        val numEntries = reader.ReadInt32()
        val pcen2 = Out<FName?>()
        val pcen = reader.Asset!!.GetParentClassExportName(pcen2)
        for (i in 0 until numEntries) {
            val rowName = reader.ReadFName()
            val nextStruct = StructPropertyData(rowName)
            nextStruct.StructType = decidedStructType
            nextStruct.Ancestry.Initialize(null, pcen, pcen2.value)
            nextStruct.Read(reader, false, 1)
            Table!!.Data.add(nextStruct)
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
        if (Table?.Data != null) {
            for (i in Table!!.Data.indices) Table!!.Data[i].ResolveAncestries(asset, ancestryNew)
        }
        super.ResolveAncestries(asset, ancestrySoFar)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        // Find an ObjectProperty named RowStruct
        var decidedStructType = FName.DefineDummy(writer.Asset, "Generic")
        for (thisData in Data!!) {
            if (thisData.Name!!.Value!!.Value == "RowStruct" && thisData is ObjectPropertyData) {
                decidedStructType = thisData.ToImport(writer.Asset!!)!!.ObjectName!!
                break
            }
        }

        if (decidedStructType.toString() == "Generic") {
            // overrides here...
            val exportClassTypeName = this.GetExportClassType()
            val exportClassType = exportClassTypeName!!.Value!!.Value
            when (exportClassType) {
                "CommonGenericInputActionDataTable" -> decidedStructType = FName.DefineDummy(writer.Asset, "CommonInputActionDataBase")
            }
        }

        writer.WriteInt32(Table!!.Data.size)
        for (i in Table!!.Data.indices) {
            val thisDataTableEntry = Table!!.Data[i]
            thisDataTableEntry.StructType = decidedStructType
            writer.Write(thisDataTableEntry.Name)
            thisDataTableEntry.Write(writer, false)
        }
    }
}
