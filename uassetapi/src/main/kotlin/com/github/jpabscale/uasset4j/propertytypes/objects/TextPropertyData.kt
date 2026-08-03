// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/TextPropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FEditorObjectVersion
import com.github.jpabscale.uasset4j.customversions.FUE5ReleaseStreamObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.EFormatArgumentType
import com.github.jpabscale.uasset4j.unrealtypes.engineenums.ERoundingMode

private fun GetCustomVersion(asset: UAsset, friendlyName: String): Int =
    asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName(friendlyName))

@JvmInline
value class ETextFlag(val value: Int) {
    companion object {
        const val Transient = 1 shl 0
        const val CultureInvariant = 1 shl 1
        const val ConvertedProperty = 1 shl 2
        const val Immutable = 1 shl 3
        const val InitializedFromString = 1 shl 4
    }
}

enum class ETransformType {
    ToLower,
    ToUpper,
}

class FNumberFormattingOptions {
    var AlwaysSign: Boolean = false
    var UseGrouping: Boolean = true
    var RoundingMode: ERoundingMode = ERoundingMode.HalfToEven
    var MinimumIntegralDigits: Int = 1
    var MaximumIntegralDigits: Int = 316
    var MinimumFractionalDigits: Int = 0
    var MaximumFractionalDigits: Int = 3

    constructor()

    constructor(reader: AssetBinaryReader) {
        val asset = reader.Asset
        if (asset != null && GetCustomVersion(asset, "FEditorObjectVersion") >= FEditorObjectVersion.AddedAlwaysSignNumberFormattingOption.ordinal)
            AlwaysSign = reader.ReadBooleanInt()
        UseGrouping = reader.ReadBooleanInt()
        RoundingMode = ERoundingMode.entries[reader.ReadByte()]
        MinimumIntegralDigits = reader.ReadInt32()
        MaximumIntegralDigits = reader.ReadInt32()
        MinimumFractionalDigits = reader.ReadInt32()
        MaximumFractionalDigits = reader.ReadInt32()
    }

    fun Write(writer: AssetBinaryWriter) {
        val asset = writer.Asset
        if (asset != null && GetCustomVersion(asset, "FEditorObjectVersion") >= FEditorObjectVersion.AddedAlwaysSignNumberFormattingOption.ordinal)
            writer.WriteBooleanInt(AlwaysSign)
        writer.WriteBooleanInt(UseGrouping)
        writer.WriteByte(RoundingMode.ordinal)
        writer.WriteInt32(MinimumIntegralDigits)
        writer.WriteInt32(MaximumIntegralDigits)
        writer.WriteInt32(MinimumFractionalDigits)
        writer.WriteInt32(MaximumFractionalDigits)
    }
}

class FFormatArgumentValue {
    var Type: EFormatArgumentType = EFormatArgumentType.Int
    var Value: Any? = null

    constructor()

    constructor(type: EFormatArgumentType, value: Any?) {
        Type = type
        Value = value
    }

    constructor(reader: AssetBinaryReader, isArgumentData: Boolean = false) {
        Type = EFormatArgumentType.entries[reader.ReadByte()]
        when (Type) {
            EFormatArgumentType.Int -> {
                val cv = reader.Asset?.let { GetCustomVersion(it, "FUE5ReleaseStreamObjectVersion") } ?: -1
                Value = if (isArgumentData && cv < FUE5ReleaseStreamObjectVersion.TextFormatArgumentData64bitSupport.ordinal) reader.ReadInt32().toLong() else reader.ReadInt64()
            }
            EFormatArgumentType.UInt -> Value = reader.ReadUInt64()
            EFormatArgumentType.Double -> Value = reader.ReadDouble()
            EFormatArgumentType.Float -> Value = reader.ReadSingle()
            EFormatArgumentType.Text -> {
                val val_ = TextPropertyData(FName.DefineDummy(reader.Asset, "Value"))
                val_.Read(reader, false, 1, 0, PropertySerializationContext.Normal)
                Value = val_
            }
            else -> throw NotImplementedError("EFormatArgumentType type " + Type.name + " is not implemented for reading")
        }
    }

    fun Write(writer: AssetBinaryWriter, isArgumentData: Boolean = false): Int {
        var sz = 0
        writer.WriteByte(Type.ordinal)
        sz += 1
        when (Type) {
            EFormatArgumentType.Int -> {
                val cv = writer.Asset?.let { GetCustomVersion(it, "FUE5ReleaseStreamObjectVersion") } ?: -1
                if (isArgumentData && cv < FUE5ReleaseStreamObjectVersion.TextFormatArgumentData64bitSupport.ordinal) {
                    writer.WriteInt32((Value as Long).toInt())
                    sz += 4
                } else {
                    writer.WriteInt64(Value as Long)
                    sz += 8
                }
            }
            EFormatArgumentType.UInt -> {
                writer.WriteUInt64(Value as Long)
                sz += 8
            }
            EFormatArgumentType.Double -> {
                writer.WriteDouble(Value as Double)
                sz += 8
            }
            EFormatArgumentType.Float -> {
                writer.WriteSingle(Value as Float)
                sz += 4
            }
            EFormatArgumentType.Text -> {
                val here = writer.position
                val val_ = Value as TextPropertyData
                val_.Write(writer, false)
                sz += writer.position - here
            }
            else -> throw NotImplementedError("EFormatArgumentType type " + Type.name + " is not implemented for writing")
        }

        return sz
    }
}

class FFormatArgumentData {
    var ArgumentName: FString? = null
    var ArgumentValue: FFormatArgumentValue? = null

    constructor()

    constructor(name: FString?, value: FFormatArgumentValue?) {
        ArgumentName = name
        ArgumentValue = value
    }

    constructor(reader: AssetBinaryReader) {
        Read(reader)
    }

    fun Read(reader: AssetBinaryReader) {
        ArgumentName = reader.ReadFString()
        ArgumentValue = FFormatArgumentValue(reader, true)
    }

    fun Write(writer: AssetBinaryWriter): Int {
        var sz = writer.Write(ArgumentName)
        sz += ArgumentValue!!.Write(writer, true)
        return sz
    }
}

class TextPropertyData : PropertyData {
    var Flags: ETextFlag = ETextFlag(0)
    var HistoryType: TextHistoryType = TextHistoryType.Base
    var TableId: FName? = null
    var Namespace: FString? = null
    var CultureInvariantString: FString? = null

    var SourceFmt: TextPropertyData? = null
    var Arguments: List<FFormatArgumentValue>? = null
    var ArgumentsData: List<FFormatArgumentData>? = null
    var TransformType: ETransformType = ETransformType.ToLower
    var SourceValue: FFormatArgumentValue? = null
    var FormatOptions: FNumberFormattingOptions? = null
    var TargetCulture: FString? = null

    var Value: FString?
        get() = GetObject<FString>()
        set(v) = SetObject(v)

    fun ShouldSerializeTableId(): Boolean = HistoryType == TextHistoryType.StringTableEntry

    override val PropertyType: FString get() = CurrentPropertyType

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }

        if (reader.Asset!!.ObjectVersion < ObjectVersion.VER_UE4_FTEXT_HISTORY) {
            CultureInvariantString = reader.ReadFString()
            if (reader.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_ADDED_NAMESPACE_AND_KEY_DATA_TO_FTEXT) {
                Namespace = reader.ReadFString()
                Value = reader.ReadFString()
            } else {
                Namespace = null
                Value = reader.ReadFString()
            }
        }

        Flags = ETextFlag(reader.ReadUInt32().toInt())

        if (reader.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_FTEXT_HISTORY) {
            val rawHistoryType = reader.ReadSByte()
            HistoryType = TextHistoryType.entries.firstOrNull { it.value == rawHistoryType }
                ?: throw NotImplementedError("Unimplemented reader for unknown TextHistoryType @ " + reader.position)

            when (HistoryType) {
                TextHistoryType.None -> {
                    Value = null
                    val asset = reader.Asset
                    if (asset != null && GetCustomVersion(asset, "FEditorObjectVersion") >= FEditorObjectVersion.CultureInvariantTextSerializationKeyStability.ordinal) {
                        val bHasCultureInvariantString = reader.ReadInt32() == 1
                        if (bHasCultureInvariantString) {
                            CultureInvariantString = reader.ReadFString()
                        }
                    }
                }
                TextHistoryType.Base -> {
                    Namespace = reader.ReadFString()
                    Value = reader.ReadFString()
                    CultureInvariantString = reader.ReadFString()
                }
                TextHistoryType.StringTableEntry -> {
                    TableId = reader.ReadFName()
                    Value = reader.ReadFString()
                }
                TextHistoryType.RawText -> {
                    Value = reader.ReadFString()
                }
                TextHistoryType.OrderedFormat -> {
                    SourceFmt = TextPropertyData(FName.DefineDummy(reader.Asset, "SourceFmt"))
                    SourceFmt!!.Read(reader, false, 1, 0, serializationContext)
                    val ArgumentsSize = reader.ReadInt32()
                    Arguments = List(ArgumentsSize) { FFormatArgumentValue(reader) }
                }
                TextHistoryType.ArgumentFormat -> {
                    SourceFmt = TextPropertyData(FName.DefineDummy(reader.Asset, "SourceFmt"))
                    SourceFmt!!.Read(reader, false, 1, 0, serializationContext)
                    val ArgumentsSize = reader.ReadInt32()
                    ArgumentsData = List(ArgumentsSize) { FFormatArgumentData(reader) }
                }
                TextHistoryType.Transform -> {
                    SourceFmt = TextPropertyData(FName.DefineDummy(reader.Asset, "SourceFmt"))
                    SourceFmt!!.Read(reader, false, 1, 0, serializationContext)
                    TransformType = ETransformType.entries[reader.ReadByte()]
                }
                TextHistoryType.AsNumber -> {
                    SourceValue = FFormatArgumentValue(reader)
                    if (reader.ReadBooleanInt()) {
                        FormatOptions = FNumberFormattingOptions(reader)
                    }
                    TargetCulture = reader.ReadFString()
                }
                else -> throw NotImplementedError("Unimplemented reader for " + HistoryType.name + " @ " + reader.position)
            }
        }
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }

        val here = writer.position

        if (writer.Asset!!.ObjectVersion < ObjectVersion.VER_UE4_FTEXT_HISTORY) {
            writer.Write(CultureInvariantString)
            if (writer.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_ADDED_NAMESPACE_AND_KEY_DATA_TO_FTEXT) {
                writer.Write(Namespace)
                writer.Write(Value)
            } else {
                writer.Write(Value)
            }
        }

        writer.WriteUInt32(Flags.value.toLong() and 0xFFFFFFFFL)

        if (writer.Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_FTEXT_HISTORY) {
            writer.WriteSByte(HistoryType.value)

            when (HistoryType) {
                TextHistoryType.None -> {
                    val asset = writer.Asset
                    if (asset != null && GetCustomVersion(asset, "FEditorObjectVersion") >= FEditorObjectVersion.CultureInvariantTextSerializationKeyStability.ordinal) {
                        if (CultureInvariantString == null || CultureInvariantString!!.Value.isNullOrEmpty()) {
                            writer.WriteInt32(0)
                        } else {
                            writer.WriteInt32(1)
                            writer.Write(CultureInvariantString)
                        }
                    }
                }
                TextHistoryType.Base -> {
                    writer.Write(Namespace)
                    writer.Write(Value)
                    writer.Write(CultureInvariantString)
                }
                TextHistoryType.StringTableEntry -> {
                    writer.Write(TableId)
                    writer.Write(Value)
                }
                TextHistoryType.RawText -> {
                    writer.Write(Value)
                }
                TextHistoryType.OrderedFormat -> {
                    SourceFmt!!.Write(writer, false, serializationContext)
                    val args = Arguments ?: emptyList()
                    writer.WriteInt32(args.size)
                    for (i in args.indices) {
                        args[i].Write(writer)
                    }
                }
                TextHistoryType.ArgumentFormat -> {
                    SourceFmt!!.Write(writer, false, serializationContext)
                    val args = ArgumentsData ?: emptyList()
                    writer.WriteInt32(args.size)
                    for (i in args.indices) {
                        args[i].Write(writer)
                    }
                }
                TextHistoryType.Transform -> {
                    SourceFmt!!.Write(writer, false, serializationContext)
                    writer.WriteByte(TransformType.ordinal)
                }
                TextHistoryType.AsNumber -> {
                    SourceValue!!.Write(writer)
                    if (FormatOptions != null) {
                        writer.WriteInt32(1)
                        FormatOptions!!.Write(writer)
                    } else {
                        writer.WriteInt32(0)
                    }
                    writer.Write(TargetCulture)
                }
                else -> throw NotImplementedError("Unimplemented writer for " + HistoryType.name)
            }
        }

        return writer.position - here
    }

    override fun toString(): String {
        if (Value == null) return "null"

        return when (HistoryType) {
            TextHistoryType.None -> "None, ${CultureInvariantString?.toString() ?: ""}"
            TextHistoryType.Base -> "Base, ${Namespace?.toString() ?: ""}, ${Value.toString()}, ${CultureInvariantString?.toString() ?: ""}"
            TextHistoryType.StringTableEntry -> "StringTableEntry, ${TableId?.toString() ?: ""}, ${Value.toString()}"
            else -> throw NotImplementedError("Unimplemented display for " + HistoryType.name)
        }
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        throw NotImplementedError("TextPropertyData.FromString is currently unimplemented")
    }

    override fun CloneInto(res: PropertyData) {
        super.CloneInto(res)
        val cloningProperty = res as TextPropertyData

        cloningProperty.TableId = this.TableId?.clone()
        cloningProperty.Namespace = this.Namespace?.clone()
        cloningProperty.CultureInvariantString = this.CultureInvariantString?.clone()
    }

    override fun CreateClone(): PropertyData = TextPropertyData()

    constructor(name: FName?) : super(name)
    constructor() : super()

    companion object {
        private val CurrentPropertyType = FString("TextProperty")
    }
}
