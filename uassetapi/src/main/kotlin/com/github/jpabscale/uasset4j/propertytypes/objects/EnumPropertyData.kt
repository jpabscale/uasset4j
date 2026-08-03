// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/EnumPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unversioned.UsmapEnumData

class EnumPropertyData : PropertyData {
    var EnumType: FName? = null
    var InnerType: FName? = null

    var Value: FName?
        get() = GetObject<FName>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        val mappings = reader.Asset!!.Mappings
        if (mappings != null) {
            val enumDat1 = mappings.TryGetPropertyData(Name, Ancestry, reader.Asset!!) as? UsmapEnumData
            if (enumDat1 != null) {
                EnumType = if (reader.Asset!!.HasUnversionedProperties) FName.DefineDummy(reader.Asset, enumDat1.Name ?: "") else FName(reader.Asset!!, enumDat1.Name ?: "")
                InnerType = if (reader.Asset!!.HasUnversionedProperties) FName.DefineDummy(reader.Asset, enumDat1.InnerType!!.Type.toString()) else FName(reader.Asset!!, enumDat1.Name ?: "")
            }
        }

        if (reader.Asset!!.HasUnversionedProperties && serializationContext.IsNormal()) {
            Value = null
            if (InnerType?.Value?.Value == "ByteProperty" || InnerType?.Value?.Value == "UInt16Property" || InnerType?.Value?.Value == "UInt32Property") {
                var enumIndice = 0L

                when (InnerType?.Value?.Value) {
                    "ByteProperty" -> {
                        enumIndice = reader.ReadByte().toLong()
                        if (enumIndice == 255L) return
                    }
                    "UInt16Property" -> {
                        enumIndice = reader.ReadUInt16().toLong()
                        if (enumIndice == 65535L) return
                    }
                    "UInt32Property" -> {
                        enumIndice = reader.ReadUInt32()
                        if (enumIndice == 4294967295L) return
                    }
                }

                val listOfValues = reader.Asset!!.Mappings!!.EnumMap.get(EnumType!!.Value!!.Value ?: "")!!.Values
                if (enumIndice < listOfValues.size.toLong()) {
                    Value = FName.DefineDummy(reader.Asset, listOfValues[enumIndice] ?: "")
                } else {
                    Value = FName.DefineDummy(reader.Asset, InvalidEnumIndexFallbackPrefix + enumIndice.toString())
                }
                return
            }

            if (InnerType?.Value?.Value == "Int8Property" || InnerType?.Value?.Value == "Int16Property" ||
                InnerType?.Value?.Value == "IntProperty" || InnerType?.Value?.Value == "Int64Property"
            ) {
                var enumIndice = 0L

                when (InnerType?.Value?.Value) {
                    "Int8Property" -> enumIndice = reader.ReadSByte().toLong()
                    "Int16Property" -> enumIndice = reader.ReadInt16().toLong()
                    "IntProperty" -> enumIndice = reader.ReadInt32().toLong()
                    "Int64Property" -> enumIndice = reader.ReadInt64()
                }

                val listOfValues = reader.Asset!!.Mappings!!.EnumMap.get(EnumType!!.Value!!.Value ?: "")!!.Values
                if (enumIndice < listOfValues.size.toLong()) {
                    Value = FName.DefineDummy(reader.Asset, listOfValues[enumIndice] ?: "")
                } else {
                    Value = FName.DefineDummy(reader.Asset, InvalidEnumIndexFallbackPrefix + enumIndice.toString())
                }
                return
            }
        }

        if (includeHeader && !reader.Asset!!.HasUnversionedProperties) {
            if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                if (PropertyTypeName == null) throw FormatException("PropertyTypeName is required to read EnumProperty with complete type names.")
                EnumType = PropertyTypeName!!.GetParameter(0).GetName()
            } else {
                EnumType = reader.ReadFName()
            }
            this.ReadEndPropertyTag(reader)
        }

        Value = reader.ReadFName()
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        val mappings = writer.Asset!!.Mappings
        if (mappings != null) {
            val enumDat1 = mappings.TryGetPropertyData(Name, Ancestry, writer.Asset!!) as? UsmapEnumData
            if (enumDat1 != null) {
                EnumType = if (writer.Asset!!.HasUnversionedProperties) FName.DefineDummy(writer.Asset, enumDat1.Name ?: "") else FName(writer.Asset!!, enumDat1.Name ?: "")
                InnerType = if (writer.Asset!!.HasUnversionedProperties) FName.DefineDummy(writer.Asset, enumDat1.InnerType!!.Type.toString()) else FName(writer.Asset!!, enumDat1.Name ?: "")
            }
        }

        if (writer.Asset!!.HasUnversionedProperties && serializationContext.IsNormal()) {
            if (ValidEnumInnerTypeList.contains(InnerType?.Value?.Value)) {
                var enumIndice = 0L
                val listOfEnums = writer.Asset!!.Mappings!!.EnumMap.get(EnumType!!.Value!!.Value ?: "")!!.Values
                val validIndices = listOfEnums.filter { it.value == Value?.Value?.Value }.map { it.key }
                if (Value == null) {
                    enumIndice = -1
                } else if (validIndices.isEmpty()) {
                    var success = false
                    val valueStr = Value?.Value?.Value
                    if (valueStr != null && valueStr.startsWith(InvalidEnumIndexFallbackPrefix)) {
                        val parsed = valueStr.substring(InvalidEnumIndexFallbackPrefix.length).toLongOrNull()
                        if (parsed != null) {
                            enumIndice = parsed
                            success = true
                        }
                    }

                    if (!success) {
                        throw FormatException("Could not serialize EnumProperty value " + (Value?.Value?.Value ?: "") + " as " + (InnerType?.Value?.Value ?: ""))
                    }
                } else {
                    enumIndice = validIndices.first()
                }

                when (InnerType?.Value?.Value) {
                    "ByteProperty" -> {
                        writer.WriteByte((enumIndice and 0xFF).toInt())
                        return 1
                    }
                    "UInt16Property" -> {
                        writer.WriteUInt16((enumIndice and 0xFFFF).toInt())
                        return 2
                    }
                    "UInt32Property" -> {
                        writer.WriteUInt32(enumIndice and 0xFFFFFFFFL)
                        return 4
                    }
                    "Int8Property" -> {
                        writer.WriteByte((enumIndice and 0xFF).toInt())
                        return 1
                    }
                    "Int16Property" -> {
                        writer.WriteInt16(enumIndice.toShort())
                        return 2
                    }
                    "IntProperty" -> {
                        writer.WriteInt32(enumIndice.toInt())
                        return 4
                    }
                    "Int64Property" -> {
                        writer.WriteInt64(enumIndice)
                        return 8
                    }
                }
            }
        }

        if (includeHeader && !writer.Asset!!.HasUnversionedProperties) {
            if (writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                writer.Write(EnumType)
            }

            this.WriteEndPropertyTag(writer)
        }
        writer.Write(Value)
        return 8
    }

    internal override fun InitializeZero(reader: AssetBinaryReader) {
        val mappings = reader.Asset!!.Mappings
        if (mappings != null) {
            val enumDat1 = mappings.TryGetPropertyData(Name, Ancestry, reader.Asset!!) as? UsmapEnumData
            if (enumDat1 != null) {
                EnumType = FName.DefineDummy(reader.Asset, enumDat1.Name ?: "")
                InnerType = FName.DefineDummy(reader.Asset, enumDat1.InnerType!!.Type.toString())
            }
        }

        if (ValidEnumInnerTypeList.contains(InnerType?.Value?.Value)) {
            val enumIndice = 0L
            val listOfValues = reader.Asset!!.Mappings!!.EnumMap.get(EnumType!!.Value!!.Value ?: "")!!.Values
            if (enumIndice == 255L) {
                Value = null
            } else if (enumIndice < listOfValues.size.toLong()) {
                Value = FName.DefineDummy(reader.Asset, listOfValues[enumIndice] ?: "")
            } else {
                Value = FName.DefineDummy(reader.Asset, InvalidEnumIndexFallbackPrefix + enumIndice.toString())
            }
        }
    }

    override fun toString(): String = Value.toString()

    override fun FromString(d: Array<String>, asset: UAsset) {
        if (d[0] != "null") {
            EnumType = if (asset.HasUnversionedProperties) FName.DefineDummy(asset, d[0]) else FName.FromString(asset, d[0])
        } else {
            EnumType = null
        }

        if (d[1] != "null") {
            Value = if (asset.HasUnversionedProperties && (ValidEnumInnerTypeList.contains(InnerType?.Value?.Value))) FName.DefineDummy(asset, d[1]) else FName.FromString(asset, d[1])
        } else {
            Value = null
        }
    }

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as EnumPropertyData
        cloningProperty.EnumType = this.EnumType?.clone()
        cloningProperty.InnerType = this.InnerType?.clone()
    }

    override fun CreateClone(): PropertyData = EnumPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("EnumProperty")
        private val ValidEnumInnerTypeList: List<String> = listOf("ByteProperty", "UInt16Property", "UInt32Property", "Int8Property", "Int16Property", "IntProperty", "Int64Property")
        val InvalidEnumIndexFallbackPrefix: String = "UASSETAPI_INVALID_ENUM_IDX_"
    }
}
