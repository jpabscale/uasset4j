// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/RawStructPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5

class RawStructPropertyData : PropertyData {
    var StructType: FName? = null
    var SerializeNone: Boolean = true
    var StructGUID: FGuid? = null

    var Value: ByteArray?
        get() = GetObject<ByteArray>()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true
    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader && !reader.Asset!!.HasUnversionedProperties) {
            if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                if (PropertyTypeName == null) throw FormatException("PropertyTypeName is required to read MapProperty with complete type names.")
                StructType = PropertyTypeName!!.GetParameter(0).GetName()
            } else {
                StructType = reader.ReadFName()
                if (reader.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_STRUCT_GUID_IN_PROPERTY_TAG) StructGUID = reader.ReadGuid()
            }
            this.ReadEndPropertyTag(reader)
        }

        Value = reader.ReadBytes(leng1.toInt())
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader && !writer.Asset!!.HasUnversionedProperties) {
            if (writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                writer.Write(StructType)
                if (writer.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_STRUCT_GUID_IN_PROPERTY_TAG) writer.WriteGuid(StructGUID!!)
            }
            this.WriteEndPropertyTag(writer)
        }

        writer.WriteBytes(Value!!)
        return Value!!.size
    }

    override fun CreateClone(): PropertyData = RawStructPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("RawStructProperty")
    }
}
