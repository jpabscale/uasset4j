// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Core/InstancedStructPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.core

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.MainSerializer
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FInstancedStructCustomVersion
import com.github.jpabscale.uasset4j.exporttypes.Export
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unversioned.FUnversionedHeader
import com.github.jpabscale.uasset4j.util.Out

class InstancedStructPropertyData : PropertyData {
    var Struct: FPackageIndex? = null
    var Version: Byte = 0
    var SerialSize: Int = 0

    var Value: StructPropertyData?
        get() = GetObject<StructPropertyData>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FInstancedStructCustomVersion")) <
            FInstancedStructCustomVersion.CustomVersionAdded.ordinal
        ) {
            val header = reader.ReadUInt32()
            if (header != LegacyEditorHeader.toLong()) throw FormatException("Incorrect InstancedStruct header value")
            Version = reader.ReadByte().toByte()
        }

        Struct = FPackageIndex(reader)
        SerialSize = reader.ReadInt32()
        Value = StructPropertyData(FName.DefineDummy(reader.Asset, "InstancedStruct"))
        if (Struct!!.IsNull()) return

        val unversionedHeader = FUnversionedHeader(reader)
        val parentModulePath = Out<FName?>()
        val parentName = Export.GetClassTypeForAncestry(Struct!!, reader.Asset!!, parentModulePath)
        while (true) {
            val bit = MainSerializer.Read(reader, null, parentName, parentModulePath.value, unversionedHeader, true) ?: break
            Value!!.Value!!.add(bit)
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        val start = writer.position.toLong()
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FInstancedStructCustomVersion")) <
            FInstancedStructCustomVersion.CustomVersionAdded.ordinal
        ) {
            writer.WriteUInt32(LegacyEditorHeader.toLong())
            writer.WriteByte(Version.toInt())
        }

        writer.WriteInt32(Struct?.Index ?: 0)
        val saved = writer.position
        writer.WriteInt32(0)
        if (Struct!!.IsNull()) return (writer.position - start).toInt()

        val parentModulePath = Out<FName?>()
        val parentName = Export.GetClassTypeForAncestry(Struct!!, writer.Asset!!, parentModulePath)
        Value!!.Ancestry.Initialize(null, parentName, parentModulePath.value)
        val data = Value!!.Value!!
        for (current in data) {
            current.Ancestry.Initialize(null, parentName, parentModulePath.value)
        }

        MainSerializer.GenerateUnversionedHeader(data, parentName, parentModulePath.value, writer.Asset!!)!!.Write(writer)

        for (current in data) {
            MainSerializer.Write(current, writer, true)
        }

        val end = writer.position
        SerialSize = end - saved - 4
        writer.position = saved
        writer.WriteInt32(SerialSize)
        writer.position = end

        return (end - start).toInt()
    }

    override fun CreateClone(): PropertyData = InstancedStructPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private const val LegacyEditorHeader: UInt = 0xABABABABu
        private val CurrentPropertyType = FString("InstancedStruct")
    }
}
