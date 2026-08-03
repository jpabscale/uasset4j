// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/BytePropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unversioned.UsmapEnumData

enum class BytePropertyType {
    Byte,
    FName,
}

class BytePropertyData : PropertyData {
    var ByteType: BytePropertyType = BytePropertyType.Byte
    var EnumType: FName? = null
    var Value: Byte? = null
    var EnumValue: FName? = null

    fun ShouldSerializeValue(): Boolean = ByteType == BytePropertyType.Byte

    fun ShouldSerializeEnumValue(): Boolean = ByteType == BytePropertyType.FName

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        ReadCustom(reader, includeHeader, leng1, leng2, true)
    }

    private fun ReadCustom(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, canRepeat: Boolean) {
        if (includeHeader && !reader.Asset!!.HasUnversionedProperties) {
            if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                if (PropertyTypeName == null) throw FormatException("PropertyTypeName is required to read ByteProperty with complete type names.")
                EnumType = PropertyTypeName!!.GetParameter(0).GetName()
            } else {
                EnumType = reader.ReadFName()
            }

            this.ReadEndPropertyTag(reader)
        }

        var useFailsafe = true
        val mappings = reader.Asset!!.Mappings
        if (mappings != null) {
            val propDat = mappings.TryGetPropertyData(Name, Ancestry, reader.Asset!!)
            if (propDat != null) {
                useFailsafe = false
                ByteType = if (propDat is UsmapEnumData) BytePropertyType.FName else BytePropertyType.Byte
            }
        }

        if (!reader.Asset!!.HasUnversionedProperties) {
            when (leng1) {
                1L -> {
                    ByteType = BytePropertyType.Byte
                    useFailsafe = false
                }
                8L -> {
                    ByteType = BytePropertyType.FName
                    useFailsafe = false
                }
            }
        }

        if (useFailsafe) {
            when (leng1) {
                0L -> {
                    val nameMapPointer = reader.ReadInt32()
                    val nameMapIndex = reader.ReadInt32()
                    reader.position -= 8

                    if (nameMapPointer >= 0 && nameMapPointer < reader.Asset!!.GetNameMapIndexList().size && nameMapIndex == 0 && !reader.Asset!!.GetNameReference(nameMapPointer).toString().contains("/")) {
                        ByteType = BytePropertyType.FName
                    } else {
                        ByteType = BytePropertyType.Byte
                    }
                }
                else -> {
                    if (canRepeat) {
                        ReadCustom(reader, false, leng2, 0, false)
                        return
                    }
                    throw FormatException("Invalid length $leng1 for ByteProperty")
                }
            }
        }

        if (ByteType == BytePropertyType.Byte) {
            Value = reader.ReadByte().toByte()
        } else if (ByteType == BytePropertyType.FName) {
            EnumValue = reader.ReadFName()
        }
    }

    internal override fun InitializeZero(reader: AssetBinaryReader) {
        Value = 0
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader && !writer.Asset!!.HasUnversionedProperties) {
            if (writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                writer.Write(EnumType)
            }
            this.WriteEndPropertyTag(writer)
        }

        when (ByteType) {
            BytePropertyType.Byte -> {
                writer.WriteByte(Value?.toInt() ?: 0)
                return 1
            }
            BytePropertyType.FName -> {
                writer.Write(EnumValue)
                return 8
            }
        }
    }

    fun GetEnumBase(): FName? = EnumType

    fun GetEnumFull(): FName? = EnumValue

    override fun toString(): String {
        if (ByteType == BytePropertyType.Byte) return (Value ?: 0).toString()
        return (Value ?: 0).toString()
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        EnumType = FName.FromString(asset, d[0])
        val res = d[1].toIntOrNull()
        if (res != null && res in 0..255) {
            ByteType = BytePropertyType.Byte
            Value = res.toByte()
        } else {
            ByteType = BytePropertyType.FName
            EnumValue = FName.FromString(asset, d[1])
        }
    }

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as BytePropertyData
        cloningProperty.ByteType = this.ByteType
        cloningProperty.EnumType = this.EnumType?.clone()
        cloningProperty.Value = this.Value
        cloningProperty.EnumValue = this.EnumValue?.clone()
    }

    override fun CreateClone(): PropertyData = BytePropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("ByteProperty")
    }
}
