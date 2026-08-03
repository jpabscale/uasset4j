// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Structs/StructPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.structs

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.FormatException
import com.github.jpabscale.uasset4j.MainSerializer
import com.github.jpabscale.uasset4j.RegistryEntry
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FEditorObjectVersion
import com.github.jpabscale.uasset4j.customversions.FSequencerObjectVersion
import com.github.jpabscale.uasset4j.exporttypes.EClassSerializationControlExtension
import com.github.jpabscale.uasset4j.propertytypes.objects.AncestryInfo
import com.github.jpabscale.uasset4j.propertytypes.objects.EOverriddenPropertyOperation
import com.github.jpabscale.uasset4j.propertytypes.objects.EPropertyTagFlags
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertyData
import com.github.jpabscale.uasset4j.propertytypes.objects.PropertySerializationContext
import com.github.jpabscale.uasset4j.propertytypes.structs.ranges.FloatRangePropertyData
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5
import com.github.jpabscale.uasset4j.unversioned.FUnversionedHeader
import com.github.jpabscale.uasset4j.unversioned.UsmapStructData
import com.github.jpabscale.uasset4j.util.Out

open class StructPropertyData : PropertyData {
    var StructType: FName? = null
    var SerializeNone: Boolean = true
    var StructGUID: FGuid = FGuid(0u, 0u, 0u, 0u)
    var SerializationControl: EClassSerializationControlExtension = EClassSerializationControlExtension(EClassSerializationControlExtension.NoExtension)
    var Operation: EOverriddenPropertyOperation = EOverriddenPropertyOperation.None

    var Value: MutableList<PropertyData>?
        get() = GetObject<MutableList<PropertyData>>()
        set(v) = SetObject(v)

    operator fun get(key: FName): PropertyData? {
        val v = Value ?: return null

        for (i in v.indices) {
            if (v[i].Name == key) return v[i]
        }
        return null
    }

    operator fun set(key: FName, value: PropertyData) {
        var v = Value
        if (v == null) {
            v = mutableListOf()
            Value = v
        }
        value.Name = key

        for (i in v.indices) {
            if (v[i].Name == key) {
                v[i] = value
                return
            }
        }

        v.add(value)
    }

    operator fun get(key: String): PropertyData? =
        FName.FromString(Name?.Asset!!, key)?.let { this[it] }

    operator fun set(key: String, value: PropertyData) {
        FName.FromString(Name?.Asset!!, key)?.let { this[it] = value }
    }

    override val PropertyType: FString get() = CurrentPropertyType

    private fun ReadOnce(reader: AssetBinaryReader, targetEntry: RegistryEntry, offset: Long, leng1: Long) {
        val data = targetEntry.Creator(Name)
        data.Offset = offset
        data.Ancestry.Initialize(Ancestry, Name)
        data.PropertyTypeName = PropertyTypeName?.GetParameter(0)
        data.Read(reader, false, leng1)
        Value = mutableListOf(data)
    }

    private fun ReadNTPL(reader: AssetBinaryReader, resetValue: Boolean = true) {
        val resultingList = if (resetValue) mutableListOf<PropertyData>() else Value!!
        var data: PropertyData? = null

        val unversionedHeader = FUnversionedHeader(reader)
        val modulePath = FName.DefineDummy(
            reader.Asset,
            (reader.Asset?.InternalAssetPath ?: "") + (if ((Ancestry.Ancestors.size) == 0) "" else "." + Ancestry.Parent),
        )
        while (MainSerializer.Read(reader, Ancestry, StructType, modulePath, unversionedHeader, true).also { data = it } != null) {
            resultingList.add(data!!)
        }

        Value = resultingList
    }

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader && !reader.Asset!!.HasUnversionedProperties) {
            if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                if (PropertyTypeName == null) throw FormatException("PropertyTypeName is required to read StructProperty with complete type names.")
                StructType = PropertyTypeName!!.GetParameter(0).GetName()
            } else {
                StructType = reader.ReadFName()
                if (reader.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_STRUCT_GUID_IN_PROPERTY_TAG) StructGUID = reader.ReadGuid()
            }

            this.ReadEndPropertyTag(reader)
        }

        if (reader.Asset!!.Mappings != null && (StructType == null || StructType?.Value?.Value == "Generic")) {
            val strucDat1 = reader.Asset!!.Mappings?.TryGetPropertyData(Name, Ancestry, reader.Asset!!)
            if (strucDat1 is UsmapStructData) {
                StructType = FName.DefineDummy(reader.Asset, strucDat1.StructType ?: "")
            }
        }

        if (reader.Asset!!.HasUnversionedProperties && StructType?.Value?.Value == null) {
            throw IllegalStateException("Unable to determine struct type for struct " + Name!!.Value!!.Value + " in class " + Ancestry.Parent!!.Value!!.Value)
        }

        var targetEntry: RegistryEntry? = null
        val structTypeVal = StructType?.Value?.Value
        if (structTypeVal != null) targetEntry = MainSerializer.PropertyTypeRegistry[structTypeVal]
        var hasCustomStructSerialization = targetEntry != null && targetEntry.HasCustomStructSerialization && serializationContext != PropertySerializationContext.StructFallback

        if (structTypeVal == "FloatRange") {
            val nextFourBytes = reader.ReadInt32()
            reader.position -= 4
            hasCustomStructSerialization = !(reader.Asset!!.HasUnversionedProperties ||
                (nextFourBytes >= 0 && nextFourBytes < reader.Asset!!.GetNameMapIndexList().size &&
                    reader.Asset!!.GetNameReference(nextFourBytes).Value?.endsWith("Bound") == true))
        }
        if (structTypeVal == "RichCurveKey" && reader.Asset!!.ObjectVersion < ObjectVersion.VER_UE4_SERIALIZE_RICH_CURVE_KEY) hasCustomStructSerialization = false
        if (structTypeVal == "MovieSceneTrackIdentifier" &&
            reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FEditorObjectVersion")) <
            FEditorObjectVersion.MovieSceneMetaDataSerialization.ordinal
        ) hasCustomStructSerialization = false
        if (structTypeVal == "MovieSceneFloatChannel" &&
            reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
            FSequencerObjectVersion.SerializeFloatChannelCompletely.ordinal
        ) hasCustomStructSerialization = false
        if (structTypeVal == "MovieSceneFloatValue" &&
            reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
            FSequencerObjectVersion.SerializeFloatChannel.ordinal
        ) hasCustomStructSerialization = false
        if (structTypeVal == "MovieSceneTangentData" &&
            reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
            FSequencerObjectVersion.SerializeFloatChannel.ordinal
        ) hasCustomStructSerialization = false
        if (structTypeVal == "FontData" &&
            reader.Asset!!.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FEditorObjectVersion")) <
            FEditorObjectVersion.AddedFontFaceAssets.ordinal
        ) hasCustomStructSerialization = false

        if (leng1 == 0L) {
            SerializeNone = false
            Value = mutableListOf()
            return
        }

        if (targetEntry != null && hasCustomStructSerialization) {
            ReadOnce(reader, targetEntry, reader.position.toLong(), leng1)
        } else {
            ReadNTPL(reader)
        }
    }

    override fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {        val ancestryNew = ancestrySoFar.clone()
        ancestryNew.SetAsParent(
            StructType,
            FName.DefineDummy(
                asset,
                (asset.InternalAssetPath ?: "") + (if (ancestrySoFar.Ancestors.size == 0) "" else "." + ancestrySoFar.Parent),
            ),
        )

        if (Value != null) {
            for (entry in Value!!) entry.ResolveAncestries(asset, ancestryNew)
        }
        super.ResolveAncestries(asset, ancestrySoFar)
    }

    private fun WriteOnce(writer: AssetBinaryWriter, serializationContext: PropertySerializationContext): Int {
        if (serializationContext == PropertySerializationContext.CanBeZero && writer.toByteArray().any { it != 0.toByte() }) return -1
        if (Value!!.size > 1) throw IllegalStateException("Structs with type " + StructType!!.Value!!.Value + " cannot have more than one entry")

        if (Value!!.size == 0) {
            Value!!.clear()
            Value!!.add(
                MainSerializer.TypeToClass(
                    StructType, Name, Ancestry, Name, null, writer.Asset!!, null, 0,
                    EPropertyTagFlags(EPropertyTagFlags.None), 0, false,
                )!!,
            )
        }
        Value!![0].Offset = writer.position.toLong()
        return Value!![0].Write(writer, false)
    }

    private fun WriteNTPL(writer: AssetBinaryWriter, serializationContext: PropertySerializationContext): Int {
        val here = writer.position

        val allDat = Value!!
        MainSerializer.GenerateUnversionedHeader(
            allDat,
            StructType,
            FName.DefineDummy(
                writer.Asset,
                (writer.Asset?.InternalAssetPath ?: "") + (if ((Ancestry.Ancestors.size) == 0) "" else "." + Ancestry.Parent),
            ),
            writer.Asset!!,
        )?.Write(writer)
        for (t in allDat) {
            if (serializationContext == PropertySerializationContext.CanBeZero && writer.toByteArray().any { it != 0.toByte() }) break
            MainSerializer.Write(t, writer, true)
        }
        if (!writer.Asset!!.HasUnversionedProperties) writer.Write(FName(writer.Asset, "None"))
        return writer.position - here
    }

    internal fun DetermineIfSerializeWithCustomStructSerialization(Asset: UAsset, serializationContext: PropertySerializationContext, targetEntry: Out<RegistryEntry?>): Boolean {
        targetEntry.value = null
        val structTypeVal = StructType?.Value?.Value
        if (structTypeVal != null) targetEntry.value = MainSerializer.PropertyTypeRegistry[structTypeVal]
        var hasCustomStructSerialization = targetEntry.value != null && targetEntry.value!!.HasCustomStructSerialization && serializationContext != PropertySerializationContext.StructFallback

        if (structTypeVal == "FloatRange") hasCustomStructSerialization = Value?.size == 1 && Value!![0] is FloatRangePropertyData
        if (structTypeVal == "RichCurveKey" && Asset.ObjectVersion < ObjectVersion.VER_UE4_SERIALIZE_RICH_CURVE_KEY) hasCustomStructSerialization = false
        if (structTypeVal == "MovieSceneTrackIdentifier" &&
            Asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FEditorObjectVersion")) <
            FEditorObjectVersion.MovieSceneMetaDataSerialization.ordinal
        ) hasCustomStructSerialization = false
        if (structTypeVal == "MovieSceneFloatChannel" &&
            Asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
            FSequencerObjectVersion.SerializeFloatChannelCompletely.ordinal
        ) hasCustomStructSerialization = false
        if (structTypeVal == "MovieSceneFloatValue" &&
            Asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
            FSequencerObjectVersion.SerializeFloatChannel.ordinal
        ) hasCustomStructSerialization = false
        if (structTypeVal == "MovieSceneTangentData" &&
            Asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FSequencerObjectVersion")) <
            FSequencerObjectVersion.SerializeFloatChannel.ordinal
        ) hasCustomStructSerialization = false
        if (structTypeVal == "FontData" &&
            Asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FEditorObjectVersion")) <
            FEditorObjectVersion.AddedFontFaceAssets.ordinal
        ) hasCustomStructSerialization = false
        return hasCustomStructSerialization
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (writer.Asset!!.Mappings != null && (StructType == null || StructType?.Value?.Value == "Generic")) {
            val strucDat1 = writer.Asset!!.Mappings?.TryGetPropertyData(Name, Ancestry, writer.Asset!!)
            if (strucDat1 is UsmapStructData) {
                StructType = FName.DefineDummy(writer.Asset, strucDat1.StructType ?: "")
            }
        }

        if (includeHeader && !writer.Asset!!.HasUnversionedProperties) {
            if (writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
                writer.Write(StructType)
                if (writer.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_STRUCT_GUID_IN_PROPERTY_TAG) writer.WriteGuid(StructGUID)
            }

            this.WriteEndPropertyTag(writer)
        }

        if (Value == null) Value = mutableListOf()

        val targetEntry = Out<RegistryEntry?>()
        val hasCustomStructSerialization = DetermineIfSerializeWithCustomStructSerialization(writer.Asset!!, serializationContext, targetEntry)
        if (targetEntry.value != null && hasCustomStructSerialization) return WriteOnce(writer, serializationContext)
        if (Value!!.size == 0 && !SerializeNone) return 0
        return WriteNTPL(writer, serializationContext)
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        if (d[4] != null && d[4] != "Generic") StructType = if (asset.HasUnversionedProperties) FName.DefineDummy(asset, d[4]) else FName.FromString(asset, d[4])
        if (StructType == null) StructType = FName.DefineDummy(asset, "Generic")
    }

    override fun CreateClone(): PropertyData = StructPropertyData()

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as StructPropertyData
        cloningProperty.StructType = this.StructType?.clone()
        cloningProperty.SerializeNone = this.SerializeNone
        cloningProperty.StructGUID = FGuid.fromBytes(this.StructGUID.toByteArray())
        cloningProperty.SerializationControl = this.SerializationControl
        cloningProperty.Operation = this.Operation

        if (this.Value != null) {
            val newData = ArrayList<PropertyData>(this.Value!!.size)
            for (i in this.Value!!.indices) {
                newData.add(this.Value!![i].clone())
            }
            cloningProperty.Value = newData
        } else {
            cloningProperty.Value = null
        }
    }

    constructor(name: FName?) : super(name) {
        Value = mutableListOf()
    }

    constructor(name: FName?, forcedType: FName?) : super(name) {
        StructType = forcedType
        Value = mutableListOf()
    }

    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("StructProperty")
    }
}
