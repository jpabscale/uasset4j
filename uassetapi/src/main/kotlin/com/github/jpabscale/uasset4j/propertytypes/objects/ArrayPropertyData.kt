// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/ArrayPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.MainSerializer
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unversioned.UsmapArrayData
import com.github.jpabscale.uasset4j.unversioned.UsmapStructData

open class ArrayPropertyData : PropertyData {
    var ArrayType: FName? = null
    var DummyStruct: StructPropertyData? = null

    internal var ShouldSerializeStructsDifferently: Boolean = true

    fun ShouldSerializeDummyStruct(): Boolean = (Value?.size ?: 0) == 0

    var Value: List<PropertyData>?
        get() = GetObject<List<PropertyData>>()
        set(v) = SetObject(v)

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader && !reader.Asset!!.HasUnversionedProperties) {
            if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                if (PropertyTypeName == null) throw FormatException("PropertyTypeName is required to read ArrayProperty with complete type names.")
                ArrayType = PropertyTypeName!!.GetParameter(0).GetName()
            } else {
                ArrayType = reader.ReadFName()
            }

            this.ReadEndPropertyTag(reader)
        }

        var arrayStructType: FName? = null
        val mappings = reader.Asset!!.Mappings
        if (mappings != null && ArrayType == null) {
            val strucDat1 = mappings.TryGetPropertyData(Name, Ancestry, reader.Asset!!) as? UsmapArrayData
            if (strucDat1 != null) {
                ArrayType = FName.DefineDummy(reader.Asset, strucDat1.InnerType!!.Type.toString())
                if (strucDat1.InnerType is UsmapStructData) arrayStructType = FName.DefineDummy(reader.Asset, (strucDat1.InnerType as UsmapStructData).StructType ?: "")
            }
        }

        if (reader.Asset!!.HasUnversionedProperties && ArrayType == null) {
            throw IllegalStateException("Unable to determine array type for array " + (Name?.Value?.Value ?: "") + " in class " + (Ancestry.Parent?.Value?.Value ?: ""))
        }

        val numEntries = reader.ReadInt32()
        if (numEntries > MainSerializer.MaxSerializedArrayLength) throw IllegalStateException("Invalid number of entries ($numEntries) for array " + (Name?.Value?.Value ?: "") + " in class " + (Ancestry.Parent?.Value?.Value ?: ""))
        if (ArrayType!!.Value!!.Value == "StructProperty" && ShouldSerializeStructsDifferently && !reader.Asset!!.HasUnversionedProperties) {
            val results = mutableListOf<PropertyData>()

            var name: FName? = this.Name
            var structLength = 1L
            var fullType = FName.DefineDummy(reader.Asset, "Generic")
            var structGUID = FGuid.fromUnsignedInts(0u, 0u, 0u, 0u)

            var isSpecialCase = false
            if (reader.Asset is UAsset) {
                isSpecialCase = reader.Asset!!.ObjectVersion == ObjectVersion.VER_UE4_INNER_ARRAY_TAG_INFO && reader.Asset!!.WillSerializeNameHashes == true
            }

            if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                fullType = PropertyTypeName!!.GetParameter(0).GetParameter(0).GetName()
            } else if (reader.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_INNER_ARRAY_TAG_INFO && !isSpecialCase) {
                name = reader.ReadFName()
                if (name.Value!!.Value == "None") {
                    Value = emptyList()
                    return
                }

                val thisArrayType = reader.ReadFName()
                if (thisArrayType.Value!!.Value == "None") {
                    Value = emptyList()
                    return
                }

                if (thisArrayType.Value!!.Value != ArrayType!!.Value!!.Value) throw FormatException("Invalid array type: " + thisArrayType.toString() + " vs " + ArrayType.toString())

                structLength = reader.ReadInt64()
                fullType = reader.ReadFName()
                structGUID = reader.ReadGuid()
                reader.ReadPropertyGuid()
            } else {
                if (arrayStructType != null) {
                    fullType = arrayStructType
                } else if (reader.Asset!!.ArrayStructTypeOverride.containsKey(Name?.Value?.Value ?: "")) {
                    fullType = FName.DefineDummy(reader.Asset, reader.Asset!!.ArrayStructTypeOverride[Name!!.Value!!.Value])
                }
            }

            if (numEntries == 0) {
                DummyStruct = StructPropertyData(name, fullType).apply {
                    StructGUID = structGUID
                }
            } else {
                val propTypeName = PropertyTypeName?.GetParameter(0)
                for (i in 0 until numEntries) {
                    val data = StructPropertyData(name, fullType)
                    data.Offset = reader.position.toLong()
                    data.Ancestry.Initialize(Ancestry, Name)
                    data.PropertyTypeName = propTypeName
                    data.Read(reader, false, structLength, 0, PropertySerializationContext.Array)
                    data.StructGUID = structGUID
                    results.add(data)
                }
                DummyStruct = results[0] as StructPropertyData
            }
            Value = results
        } else {
            if (numEntries == 0) {
                Value = emptyList()
                return
            }

            var averageSizeEstimate = ((leng1 - 4) / numEntries).toInt()
            if (averageSizeEstimate <= 0) averageSizeEstimate = 1

            val propTypeName = PropertyTypeName?.GetParameter(0)
            val results = mutableListOf<PropertyData>()
            for (i in 0 until numEntries) {
                val element = MainSerializer.TypeToClass(ArrayType, FName.DefineDummy(reader.Asset, i.toString(), Int.MIN_VALUE), Ancestry, Name, null, reader.Asset!!, propertyTypeName = propTypeName)!!
                element.Offset = reader.position.toLong()
                if (element is StructPropertyData) element.StructType = if (arrayStructType == null) FName.DefineDummy(reader.Asset, "Generic") else arrayStructType
                element.Read(reader, false, averageSizeEstimate.toLong(), 0, PropertySerializationContext.Array)
                results.add(element)
            }

            Value = results
        }
    }

    override fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        val ancestryNew = ancestrySoFar.clone()
        ancestryNew.SetAsParent(Name)

        val v = Value
        if (v != null) {
            for (i in v.indices) v[i].ResolveAncestries(asset, ancestryNew)
        }
        super.ResolveAncestries(asset, ancestrySoFar)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (Value == null) Value = emptyList()
        val value = Value!!

        if (value.size > 0) {
            ArrayType = if (writer.Asset!!.HasUnversionedProperties) FName.DefineDummy(writer.Asset, value[0].PropertyType!!) else FName(writer.Asset!!, value[0].PropertyType!!)
        }

        var arrayStructType: FName? = null
        val mappings = writer.Asset!!.Mappings
        if (mappings != null && ArrayType == null) {
            val strucDat1 = mappings.TryGetPropertyData(Name, Ancestry, writer.Asset!!) as? UsmapArrayData
            if (strucDat1 != null) {
                ArrayType = FName.DefineDummy(writer.Asset, strucDat1.InnerType!!.Type.toString())
                if (strucDat1.InnerType is UsmapStructData) arrayStructType = FName.DefineDummy(writer.Asset, (strucDat1.InnerType as UsmapStructData).StructType ?: "")
            }
        }

        if (writer.Asset!!.HasUnversionedProperties && ArrayType == null) {
            throw IllegalStateException("Unable to determine array type for array " + (Name?.Value?.Value ?: "") + " in class " + (Ancestry.Parent?.Value?.Value ?: ""))
        }

        if (includeHeader && !writer.Asset!!.HasUnversionedProperties) {
            if (writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                writer.Write(ArrayType)
            }
            this.WriteEndPropertyTag(writer)
        }

        val here = writer.position
        writer.WriteInt32(value.size)
        if (ArrayType?.Value?.Value == "StructProperty" && ShouldSerializeStructsDifferently && !writer.Asset!!.HasUnversionedProperties) {
            if (value.isEmpty() && DummyStruct == null) {
                if (arrayStructType == null && writer.Asset!!.ArrayStructTypeOverride.containsKey(Name?.Value?.Value ?: "")) {
                    arrayStructType = FName.DefineDummy(writer.Asset, writer.Asset!!.ArrayStructTypeOverride[Name!!.Value!!.Value])
                }

                if (arrayStructType == null) {
                    throw IllegalStateException("DummyStruct is null within empty StructProperty array \"" + (Name?.Value?.Value ?: "") + "\" in class \"" + (Ancestry.Parent?.Value?.Value ?: "") + "\"")
                }

                DummyStruct = StructPropertyData(this.Name, arrayStructType).apply {
                    StructGUID = FGuid.fromUnsignedInts(0u, 0u, 0u, 0u)
                }
            }
            if (value.size > 0) DummyStruct = value[0] as StructPropertyData

            val fullType = DummyStruct!!.StructType

            var lengthLoc = -1

            var isSpecialCase = false
            if (writer.Asset is UAsset) {
                isSpecialCase = writer.Asset!!.ObjectVersion == ObjectVersion.VER_UE4_INNER_ARRAY_TAG_INFO && writer.Asset!!.WillSerializeNameHashes == true
            }

            if (writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME && writer.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_INNER_ARRAY_TAG_INFO && !isSpecialCase) {
                writer.Write(DummyStruct!!.Name)
                writer.Write(FName(writer.Asset!!, "StructProperty"))
                lengthLoc = writer.position
                writer.WriteInt64(0)
                writer.Write(fullType)
                if (writer.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_STRUCT_GUID_IN_PROPERTY_TAG) writer.WriteGuid(DummyStruct!!.StructGUID)
                if (writer.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_PROPERTY_GUID_IN_PROPERTY_TAG) writer.WriteByte(0)
            }

            for (i in value.indices) {
                (value[i] as StructPropertyData).StructType = fullType
                value[i].Offset = writer.position.toLong()
                value[i].Write(writer, false)
            }

            if (writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME && writer.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_INNER_ARRAY_TAG_INFO && !isSpecialCase) {
                val fullLen = writer.position - lengthLoc
                val newLoc = writer.position
                writer.position = lengthLoc
                writer.WriteInt32(fullLen - 32 - (if (includeHeader) 1 else 0))
                writer.position = newLoc
            }
        } else {
            for (i in value.indices) {
                value[i].Offset = writer.position.toLong()
                value[i].Write(writer, false, PropertySerializationContext.Array)
            }
        }

        return writer.position - here
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        ArrayType = FName.FromString(asset, d[4])
        if (!d[0].isNullOrBlank()) {
            DummyStruct = if (d[0] == FString.NullCase) {
                null
            } else {
                StructPropertyData(this.Name, FName.FromString(asset, d[0])!!).apply {
                    StructGUID = this@ArrayPropertyData.DummyStruct?.StructGUID ?: FGuid.fromUnsignedInts(0u, 0u, 0u, 0u)
                }
            }
        }
    }

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as ArrayPropertyData
        cloningProperty.ArrayType = this.ArrayType?.clone()
        cloningProperty.DummyStruct = this.DummyStruct?.clone() as? StructPropertyData
        cloningProperty.Value = this.Value?.toList()
        cloningProperty.ShouldSerializeStructsDifferently = this.ShouldSerializeStructsDifferently
    }

    override fun CreateClone(): PropertyData = ArrayPropertyData()

    constructor(name: FName?) : super(name) {
        Value = emptyList()
    }

    constructor() : super() {
        Value = emptyList()
    }

    companion object {
        private val CurrentPropertyType = FString("ArrayProperty")
    }
}
