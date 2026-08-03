// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/Engine/SmartNamePropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs.engine

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.UAPUtils
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FAnimPhysObjectVersion
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString

class SmartNamePropertyData : PropertyData {
    var DisplayName: FName? = null
    var SmartNameID: Int = 0
    var TempGUID: FGuid? = null

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        DisplayName = reader.ReadFName()
        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FAnimPhysObjectVersion")) <
            FAnimPhysObjectVersion.RemoveUIDFromSmartNameSerialize.ordinal
        ) {
            SmartNameID = reader.ReadUInt16()
        }
        if (reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FAnimPhysObjectVersion")) <
            FAnimPhysObjectVersion.SmartNameRefactorForDeterministicCooking.ordinal && !reader.Asset!!.IsFilterEditorOnly
        ) {
            TempGUID = reader.ReadGuid()
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        val here = writer.position

        writer.Write(DisplayName)
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FAnimPhysObjectVersion")) <
            FAnimPhysObjectVersion.RemoveUIDFromSmartNameSerialize.ordinal
        ) {
            writer.WriteUInt16(SmartNameID)
        }
        if (writer.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FAnimPhysObjectVersion")) <
            FAnimPhysObjectVersion.SmartNameRefactorForDeterministicCooking.ordinal && !writer.Asset!!.IsFilterEditorOnly
        ) {
            writer.WriteGuid(TempGUID!!)
        }

        return writer.position - here
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        DisplayName = FName.FromString(asset, d[0])
        d[1].toIntOrNull()?.let { SmartNameID = it }
        TempGUID = UAPUtils.ConvertToGUID(d[2])
    }

    override fun toString(): String = "(" + ")"

    override fun CreateClone(): PropertyData = SmartNamePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("SmartName")
    }
}
