// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/MetaDataExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FEditorObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class ObjectMetaDataEntry {
    var Import: Int = 0
    var MetaData: LinkedHashMap<FName, FString?> = LinkedHashMap()

    constructor(import: Int, metaData: LinkedHashMap<FName, FString?>) {
        Import = import
        MetaData = metaData
    }

    constructor()
}

class MetaDataExport : NormalExport {
    var ObjectMetaData: MutableList<ObjectMetaDataEntry>? = null
    var RootMetaData: LinkedHashMap<FName, FString?>? = null

    constructor(superExport: Export) : super(superExport)

    constructor(asset: UAsset?, extras: ByteArray?) : super(asset, extras)

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)

        ObjectMetaData = mutableListOf()
        val objectMetaDataMapCount = reader.ReadInt32()
        for (i in 0 until objectMetaDataMapCount) {
            val import = reader.ReadInt32()
            val metaDataCount = reader.ReadInt32()
            val metaData = LinkedHashMap<FName, FString?>()
            for (j in 0 until metaDataCount) {
                val key = reader.ReadFName()
                val value = reader.ReadFString()
                metaData[key] = value
            }
            ObjectMetaData!!.add(ObjectMetaDataEntry(import, metaData))
        }

        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FEditorObjectVersion")) >= FEditorObjectVersion.RootMetaDataSupport.ordinal) {
            RootMetaData = LinkedHashMap()
            val rootMetaDataMapCount = reader.ReadInt32()
            for (i in 0 until rootMetaDataMapCount) {
                val key = reader.ReadFName()
                val value = reader.ReadFString()
                RootMetaData!![key] = value
            }
        }
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        writer.WriteInt32(ObjectMetaData!!.size)
        for (entry in ObjectMetaData!!) {
            writer.WriteInt32(entry.Import)
            writer.WriteInt32(entry.MetaData.size)
            for ((key, value) in entry.MetaData) {
                writer.Write(key)
                writer.Write(value)
            }
        }

        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FEditorObjectVersion")) >= FEditorObjectVersion.RootMetaDataSupport.ordinal) {
            writer.WriteInt32(RootMetaData!!.size)
            for ((key, value) in RootMetaData!!) {
                writer.Write(key)
                writer.Write(value)
            }
        }
    }
}
