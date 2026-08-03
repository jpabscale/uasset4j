// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/NormalExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.MainSerializer
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.propertytypes.objects.EOverriddenPropertyOperation
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.unrealtypes.EObjectFlags
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unversioned.FUnversionedHeader
import com.github.jpabscale.uasset4j.util.Out

/**
 * A regular export representing a UObject, with no special serialization.
 */
open class NormalExport : Export {
    var Data: MutableList<PropertyData>? = null
    var ObjectGuid: FGuid? = null
    var SerializationControl: EClassSerializationControlExtension = EClassSerializationControlExtension(EClassSerializationControlExtension.NoExtension)
    var Operation: EOverriddenPropertyOperation = EOverriddenPropertyOperation.None
    var HasLeadingFourNullBytes: Boolean = false

    open operator fun get(key: FName): PropertyData? {
        for (i in Data!!.indices) {
            if (Data!![i].Name == key) return Data!![i]
        }
        return null
    }

    open operator fun set(key: FName, value: PropertyData) {
        value.Name = key

        for (i in Data!!.indices) {
            if (Data!![i].Name == key) {
                Data!![i] = value
                return
            }
        }

        Data!!.add(value)
    }

    open operator fun get(key: String): PropertyData? {
        return this[FName.FromString(Asset!!, key)!!]
    }

    open operator fun set(key: String, value: PropertyData) {
        this[FName.FromString(Asset!!, key)!!] = value
    }

    operator fun get(index: Int): PropertyData {
        return Data!![index]
    }

    operator fun set(index: Int, value: PropertyData) {
        Data!![index] = value
    }

    constructor(superExport: Export) {
        Asset = superExport.Asset
        Extras = superExport.Extras
    }

    constructor(asset: UAsset?, extras: ByteArray?) : super(asset, extras)

    constructor(data: MutableList<PropertyData>, asset: UAsset?, extras: ByteArray?) : super(asset, extras) {
        Data = data
    }

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        // 5.4-specific problem; unclear why this occurs
        if (reader.Asset!!.ObjectVersionUE5 > ObjectVersionUE5.DATA_RESOURCES &&
            reader.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.ASSETREGISTRY_PACKAGEBUILDDEPENDENCIES &&
            !ObjectFlags.hasFlag(EObjectFlags.RF_ClassDefaultObject.value)
        ) {
            val dummy = reader.ReadInt32()
            if (dummy == 0) {
                HasLeadingFourNullBytes = true
            } else {
                HasLeadingFourNullBytes = false
                reader.position -= 4
            }
        }

        Data = mutableListOf()
        var bit: PropertyData?

        val unversionedHeader = FUnversionedHeader(reader)
        if (!reader.Asset!!.HasUnversionedProperties && reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_EXTENSION_AND_OVERRIDABLE_SERIALIZATION) {
            SerializationControl = EClassSerializationControlExtension(reader.ReadByte().toByte())

            if (SerializationControl.HasFlag(EClassSerializationControlExtension.OverridableSerializationInformation)) {
                Operation = EOverriddenPropertyOperation.entries[reader.ReadByte()]
            }
        }
        val parentModulePath = Out<FName?>()
        val parentName = GetClassTypeForAncestry(reader.Asset, parentModulePath)
        while (MainSerializer.Read(reader, null, parentName, parentModulePath.value, unversionedHeader, true).also { bit = it } != null) {
            Data!!.add(bit!!)
        }

        ObjectGuid = null
        if (!this.ObjectFlags.hasFlag(EObjectFlags.RF_ClassDefaultObject.value) && reader.ReadBooleanInt()) ObjectGuid = reader.ReadGuid()
    }

    override fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        val ancestryNew = ancestrySoFar.clone()
        val modulePath = Out<FName?>()
        ancestryNew.SetAsParent(GetClassTypeForAncestry(asset, modulePath), modulePath.value)

        if (Data != null) {
            for (i in Data!!.indices) Data!![i].ResolveAncestries(asset, ancestryNew)
        }
        super.ResolveAncestries(asset, ancestrySoFar)
    }

    override fun Write(writer: AssetBinaryWriter) {
        // 5.4-specific problem; unclear why this occurs
        if (HasLeadingFourNullBytes &&
            writer.Asset!!.ObjectVersionUE5 > ObjectVersionUE5.DATA_RESOURCES &&
            writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.ASSETREGISTRY_PACKAGEBUILDDEPENDENCIES &&
            !ObjectFlags.hasFlag(EObjectFlags.RF_ClassDefaultObject.value)
        ) {
            writer.WriteInt32(0)
        }

        val parentModulePath = Out<FName?>()
        val parentName = GetClassTypeForAncestry(writer.Asset, parentModulePath)

        MainSerializer.GenerateUnversionedHeader(Data!!, parentName, parentModulePath.value, writer.Asset!!)?.Write(writer)

        if (!writer.Asset!!.HasUnversionedProperties && writer.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_EXTENSION_AND_OVERRIDABLE_SERIALIZATION) {
            writer.WriteByte(SerializationControl.value.toInt() and 0xFF)

            if (SerializationControl.HasFlag(EClassSerializationControlExtension.OverridableSerializationInformation)) {
                writer.WriteByte(Operation.ordinal)
            }
        }

        for (j in 0 until Data!!.size) {
            val current = Data!![j]
            MainSerializer.Write(current, writer, true)
        }
        if (!writer.Asset!!.HasUnversionedProperties) writer.Write(FName(writer.Asset, "None"))

        if (this.ObjectFlags.hasFlag(EObjectFlags.RF_ClassDefaultObject.value)) return
        val guid = ObjectGuid
        if (guid == null) {
            writer.WriteInt32(0)
        } else {
            writer.WriteInt32(1)
            writer.WriteGuid(guid)
        }
    }
}
