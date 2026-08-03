// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/UserDefinedStructExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.MainSerializer
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FFrameworkObjectVersion
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.propertytypes.objects.BytePropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.EOverriddenPropertyOperation
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.EObjectFlags
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unversioned.FUnversionedHeader
import com.github.jpabscale.uasset4j.unversioned.Usmap

class UserDefinedStructExport : StructExport {
    var StructFlags: Long = 0
    var StructData: MutableList<PropertyData> = mutableListOf()
    var SerializationControl2: EClassSerializationControlExtension = EClassSerializationControlExtension(EClassSerializationControlExtension.NoExtension)
    var Operation2: EOverriddenPropertyOperation = EOverriddenPropertyOperation.None

    constructor(superExport: Export) : super(superExport)

    constructor(asset: UAsset?, extras: ByteArray?) : super(asset, extras)

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)

        val schemas = reader.Asset!!.Mappings?.Schemas
        val objectName = this.ObjectName?.Value?.Value
        if (schemas != null && objectName != null) {
            val newSchema = Usmap.GetSchemaFromStructExport(this, reader.Asset!!.Mappings?.AreFNamesCaseInsensitive ?: true)
            schemas.put(objectName, newSchema)
        }

        val statusProperty = Data!!.firstOrNull { it.Name?.Value?.Value == "Status" }
        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFrameworkObjectVersion")) < FFrameworkObjectVersion.UserDefinedStructsStoreDefaultInstance.ordinal ||
            (statusProperty is BytePropertyData && statusProperty.EnumValue?.Value?.Value != "UDSS_UpToDate")
        ) return

        if (this.ObjectFlags.hasFlag(EObjectFlags.RF_ClassDefaultObject.value)) return

        StructFlags = reader.ReadUInt32()

        val unversionedHeader = FUnversionedHeader(reader)
        var bit: PropertyData?
        while (MainSerializer.Read(reader, null, this.ObjectName, FName.DefineDummy(reader.Asset, FString.FromString(reader.Asset!!.InternalAssetPath)), unversionedHeader, true).also { bit = it } != null) {
            StructData.add(bit!!)
        }
    }

    override fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        val ancestryNew = ancestrySoFar.clone()
        ancestryNew.SetAsParent(this.ObjectName, null)

        for (i in StructData.indices) StructData[i].ResolveAncestries(asset, ancestryNew)
        super.ResolveAncestries(asset, ancestrySoFar)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        val statusProperty = Data!!.firstOrNull { it.Name?.Value?.Value == "Status" }
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFrameworkObjectVersion")) < FFrameworkObjectVersion.UserDefinedStructsStoreDefaultInstance.ordinal ||
            (statusProperty is BytePropertyData && statusProperty.EnumValue?.Value?.Value != "UDSS_UpToDate")
        ) return

        if (this.ObjectFlags.hasFlag(EObjectFlags.RF_ClassDefaultObject.value)) return

        writer.WriteUInt32(StructFlags)

        MainSerializer.GenerateUnversionedHeader(StructData, this.ObjectName, FName.DefineDummy(writer.Asset, FString.FromString(writer.Asset!!.InternalAssetPath)), writer.Asset!!)?.Write(writer)
        for (j in StructData.indices) {
            MainSerializer.Write(StructData[j], writer, true)
        }
        if (!writer.Asset!!.HasUnversionedProperties) writer.Write(FName(writer.Asset, "None"))
    }
}
