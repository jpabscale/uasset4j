// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/MapPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.MainSerializer
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.propertytypes.structs.StructPropertyData
import com.github.jpabscale.uasset4j.unrealtypes.EngineVersion
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unversioned.UsmapMapData
import com.github.jpabscale.uasset4j.unversioned.UsmapStructData

class MapPropertyData : PropertyData {
    var Value: LinkedHashMap<PropertyData, PropertyData> = LinkedHashMap()

    var KeyType: FName? = null

    var ValueType: FName? = null

    fun ShouldSerializeKeyType(): Boolean = Value.isEmpty()

    fun ShouldSerializeValueType(): Boolean = Value.isEmpty()

    var KeysToRemove: List<PropertyData>? = null

    override val PropertyType: FString get() = CurrentPropertyType

    private fun MapTypeToClass(type: FName?, name: FName?, reader: AssetBinaryReader, leng: Int, includeHeader: Boolean, isKey: Boolean): PropertyData {
        when (type!!.Value!!.Value) {
            "StructProperty" -> {
                var strucType: FName? = null
                val propertyTypeNameLocal = PropertyTypeName?.GetParameter(if (isKey) 0 else 1)
                if (!reader.Asset!!.HasUnversionedProperties && reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                    strucType = propertyTypeNameLocal?.GetParameter(0)?.GetName()
                } else if (reader.Asset!!.Mappings != null && reader.Asset!!.Mappings!!.TryGetPropertyData(Name, Ancestry, reader.Asset!!) is UsmapMapData) {
                    val mapDat = reader.Asset!!.Mappings!!.TryGetPropertyData(Name, Ancestry, reader.Asset!!) as UsmapMapData
                    if (isKey && mapDat.InnerType is UsmapStructData) {
                        strucType = FName.DefineDummy(reader.Asset, (mapDat.InnerType as UsmapStructData).StructType ?: "")
                    } else if (mapDat.ValueType is UsmapStructData) {
                        strucType = FName.DefineDummy(reader.Asset, (mapDat.ValueType as UsmapStructData).StructType ?: "")
                    }
                } else if (reader.Asset!!.MapStructTypeOverride.containsKey(name!!.Value!!.Value)) {
                    if (isKey) {
                        strucType = FName.DefineDummy(reader.Asset, reader.Asset!!.MapStructTypeOverride[name.Value!!.Value]!!.first)
                    } else {
                        strucType = FName.DefineDummy(reader.Asset, reader.Asset!!.MapStructTypeOverride[name.Value!!.Value]!!.second)
                        if (name.Value!!.Value == "TrackSignatureToTrackIdentifier" && reader.Asset!!.GetEngineVersion() <= EngineVersion.VER_UE4_18)
                            strucType = FName.DefineDummy(reader.Asset, "Generic")
                    }
                }

                if (strucType?.Value == null) strucType = FName.DefineDummy(reader.Asset, "Generic")

                val data = StructPropertyData(name, strucType)
                data.Ancestry.Initialize(Ancestry, Name)
                data.Offset = reader.position.toLong()
                data.PropertyTypeName = propertyTypeNameLocal
                data.Read(reader, false, 1L, 0, PropertySerializationContext.Map)
                return data
            }
            else -> {
                val res = MainSerializer.TypeToClass(type, name, Ancestry, Name, null, reader.Asset!!, null, leng, propertyTypeName = PropertyTypeName?.GetParameter(0))!!
                res.Ancestry.Initialize(Ancestry, Name)
                res.Read(reader, includeHeader, leng.toLong(), 0, PropertySerializationContext.Map)
                return res
            }
        }
    }

    private fun ReadRawMap(reader: AssetBinaryReader, type1: FName?, type2: FName?, numEntries: Int): LinkedHashMap<PropertyData, PropertyData> {
        val resultingDict = LinkedHashMap<PropertyData, PropertyData>()
        for (i in 0 until numEntries) {
            val data1 = MapTypeToClass(type1, Name, reader, 0, false, true)
            val data2 = MapTypeToClass(type2, Name, reader, 0, false, false)

            resultingDict[data1] = data2
        }
        return resultingDict
    }

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        var type1: FName? = null
        var type2: FName? = null
        if (includeHeader && !reader.Asset!!.HasUnversionedProperties) {
            if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                if (PropertyTypeName == null) throw FormatException("PropertyTypeName is required to read MapProperty with complete type names.")
                type1 = PropertyTypeName!!.GetParameter(0).GetName()
                type2 = PropertyTypeName!!.GetParameter(1).GetName()
            } else {
                type1 = reader.ReadFName()
                type2 = reader.ReadFName()
            }

            this.ReadEndPropertyTag(reader)
        }

        if (reader.Asset!!.Mappings != null && type1 == null && type2 == null) {
            val strucDat1 = reader.Asset!!.Mappings!!.TryGetPropertyData(Name, Ancestry, reader.Asset!!) as? UsmapMapData
            if (strucDat1 != null) {
                type1 = FName.DefineDummy(reader.Asset, strucDat1.InnerType!!.Type.toString())
                type2 = FName.DefineDummy(reader.Asset, strucDat1.ValueType!!.Type.toString())
            }
        }

        val numKeysToRemove = reader.ReadInt32()
        if (numKeysToRemove > MainSerializer.MaxSerializedArrayLength) throw FormatException("KeysToRemove length ($numKeysToRemove) exceeds max length (${MainSerializer.MaxSerializedArrayLength})")
        KeysToRemove = List(numKeysToRemove) { MapTypeToClass(type1, Name, reader, 0, false, true) }

        val numEntries = reader.ReadInt32()
        if (numEntries > MainSerializer.MaxSerializedArrayLength) throw FormatException("Value length ($numEntries) exceeds max length (${MainSerializer.MaxSerializedArrayLength})")
        if (numEntries == 0) {
            KeyType = type1
            ValueType = type2
        }

        Value = ReadRawMap(reader, type1, type2, numEntries)
    }

    override fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        val ancestryNew = ancestrySoFar.clone()
        ancestryNew.SetAsParent(Name)

        for (entry in Value) {
            entry.key.ResolveAncestries(asset, ancestryNew)
            entry.value.ResolveAncestries(asset, ancestryNew)
        }
        super.ResolveAncestries(asset, ancestrySoFar)
    }

    private fun WriteRawMap(writer: AssetBinaryWriter, map: LinkedHashMap<PropertyData, PropertyData>?, serializationContext: PropertySerializationContext) {
        if (map == null) return
        for (entry in map) {
            entry.key.Offset = writer.position.toLong()
            entry.key.Write(writer, false, PropertySerializationContext.Map)
            entry.value.Offset = writer.position.toLong()
            entry.value.Write(writer, false, PropertySerializationContext.Map)
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader && !writer.Asset!!.HasUnversionedProperties) {
            if (writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                if (Value.isNotEmpty()) {
                    writer.Write(FName(writer.Asset!!, Value.keys.elementAt(0).PropertyType!!))
                    writer.Write(FName(writer.Asset!!, Value.values.elementAt(0).PropertyType!!))
                } else {
                    writer.Write(KeyType)
                    writer.Write(ValueType)
                }
            }
            this.WriteEndPropertyTag(writer)
        }

        val here = writer.position
        writer.WriteInt32(KeysToRemove?.size ?: 0)
        val keysToRemove = KeysToRemove
        if (keysToRemove != null) {
            for (i in keysToRemove.indices) {
                val entry = keysToRemove[i]
                entry.Offset = writer.position.toLong()
                entry.Write(writer, false, PropertySerializationContext.Array)
            }
        }

        writer.WriteInt32(Value.size)
        WriteRawMap(writer, Value, serializationContext)
        return writer.position - here
    }

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as MapPropertyData

        val v = this.Value
        if (v.isNotEmpty()) {
            val newDict = LinkedHashMap<PropertyData, PropertyData>()
            for (entry in v) {
                newDict[entry.key.clone()] = entry.value.clone()
            }
            cloningProperty.Value = newDict
        } else {
            cloningProperty.Value = LinkedHashMap()
        }

        cloningProperty.KeysToRemove = this.KeysToRemove?.toList()
        cloningProperty.KeyType = this.KeyType?.clone()
        cloningProperty.ValueType = this.ValueType?.clone()
    }

    override fun CreateClone(): PropertyData = MapPropertyData()

    constructor(name: FName?) : super(name) {
        Value = LinkedHashMap()
    }

    constructor() : super() {
        Value = LinkedHashMap()
    }

    companion object {
        private val CurrentPropertyType = FString("MapProperty")
    }
}
