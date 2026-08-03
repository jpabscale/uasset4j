// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/PropertyTypes/Objects/PropertyData.cs
package com.github.jpabscale.uasset4j.propertytypes.objects

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.unrealtypes.FGuid
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPropertyTypeName
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersionUE5

enum class PropertySerializationContext {
    Normal,
    Array,
    Map,
    StructFallback,
    CanBeZero,
}

fun PropertySerializationContext.IsNormal(): Boolean =
    this == PropertySerializationContext.Normal || this == PropertySerializationContext.CanBeZero

@JvmInline
value class EPropertyTagFlags(val value: Int) {
    fun HasFlag(flag: Int): Boolean = (value and flag) == flag

    companion object {
        const val None = 0x00
        const val HasArrayIndex = 0x01
        const val HasPropertyGuid = 0x02
        const val HasPropertyExtensions = 0x04
        const val HasBinaryOrNativeSerialize = 0x08
        const val BoolTrue = 0x10
        const val SkippedSerialize = 0x20
    }
}

@JvmInline
value class EPropertyTagExtension(val value: Byte) {
    fun HasFlag(flag: Byte): Boolean = (value.toInt() and flag.toInt()) == flag.toInt()

    companion object {
        const val NoExtension: Byte = 0x00
        const val ReserveForFutureUse: Byte = 0x01
        const val OverridableInformation: Byte = 0x02
    }
}

enum class EOverriddenPropertyOperation {
    None,
    Modified,
    Replace,
    Add,
    Remove,
}

class AncestryInfo {
    var Ancestors: MutableList<FName> = mutableListOf()

    var Parent: FName?
        get() {
            if (Ancestors.isEmpty()) return null
            return Ancestors[Ancestors.size - 1]
        }
        set(value) {
            Ancestors[Ancestors.size - 1] = value!!
        }

    fun clone(): AncestryInfo {
        val res = AncestryInfo()
        res.Ancestors.addAll(Ancestors)
        return res
    }

    fun CloneWithoutParent(): AncestryInfo {
        val res = clone()
        res.Ancestors.removeAt(res.Ancestors.size - 1)
        return res
    }

    fun Initialize(ancestors: AncestryInfo?, dad: FName?, modulePath: FName? = null) {
        Ancestors.clear()
        if (ancestors != null) Ancestors.addAll(ancestors.Ancestors)
        SetAsParent(dad, modulePath)
    }

    fun SetAsParent(dad: FName?, modulePath: FName? = null) {
        if (dad != null) {
            Ancestors.add(
                if (modulePath?.Value?.Value.isNullOrEmpty()) {
                    dad
                } else {
                    FName.DefineDummy(null, modulePath.toString() + "." + dad.toString())
                }
            )
        }
    }
}

/** Generic Unreal property class. */
abstract class PropertyData {
    /** The name of this property. */
    var Name: FName? = null

    /** The ancestry of this property. Not serialized. */
    var Ancestry: AncestryInfo = AncestryInfo()

    /** The array index of this property. */
    var ArrayIndex: Int = 0

    /** An optional property GUID. Nearly always null. */
    var PropertyGuid: FGuid? = null

    /** Whether or not this property is "zero". */
    var IsZero: Boolean = false

    var PropertyTagFlags: EPropertyTagFlags = EPropertyTagFlags(EPropertyTagFlags.None)

    var PropertyTypeName: FPropertyTypeName? = null

    var PropertyTagExtensions: EPropertyTagExtension = EPropertyTagExtension(EPropertyTagExtension.NoExtension)

    var OverrideOperation: EOverriddenPropertyOperation = EOverriddenPropertyOperation.None
    var bExperimentalOverridableLogic: Boolean = false

    fun ShouldSerializeOverrideOperation(): Boolean =
        PropertyTagExtensions.HasFlag(EPropertyTagExtension.OverridableInformation)

    fun ShouldSerializebExperimentalOverridableLogic(): Boolean =
        PropertyTagExtensions.HasFlag(EPropertyTagExtension.OverridableInformation)

    /** The offset of this property on disk. For the user only. */
    var Offset: Long = -1

    /** An optional tag for the user only. */
    var Tag: Any? = null

    protected var _rawValue: Any? = null
    open var RawValue: Any?
        get() {
            if (_rawValue == null && DefaultValue != null) _rawValue = DefaultValue
            return _rawValue
        }
        set(value) {
            _rawValue = value
        }

    fun SetObject(value: Any?) {
        RawValue = value
    }

    fun <T> GetObject(): T? {
        if (RawValue == null) return null
        return RawValue as T
    }

    constructor(name: FName?) {
        Name = name
    }

    constructor()

    /** Determines whether this property should be registered in the property registry. */
    open val ShouldBeRegistered: Boolean get() = true

    /** Determines whether this property has custom serialization within a StructProperty. */
    open val HasCustomStructSerialization: Boolean get() = false

    /** The type of this property as an FString. */
    open val PropertyType: FString? get() = FallbackPropertyType

    /** The default value of this property. */
    open val DefaultValue: Any? get() = null

    /** Reads out a property from a BinaryReader. */
    open fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long = 0, serializationContext: PropertySerializationContext = PropertySerializationContext.Normal) {
    }

    /** Resolves the ancestry of all child properties of this property. */
    open fun ResolveAncestries(asset: UAsset, ancestrySoFar: AncestryInfo) {
        Ancestry = ancestrySoFar
    }

    /** Complete reading the property tag of this property. */
    protected open fun ReadEndPropertyTag(reader: AssetBinaryReader) {
        if (reader.Asset!!.HasUnversionedProperties) return

        if (reader.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
            PropertyGuid = reader.ReadPropertyGuid()
        } else if (PropertyTagFlags.HasFlag(EPropertyTagFlags.HasPropertyGuid)) {
            PropertyGuid = reader.ReadGuid()
        }

        if (reader.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_EXTENSION_AND_OVERRIDABLE_SERIALIZATION) {
            if (reader.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME ||
                PropertyTagFlags.HasFlag(EPropertyTagFlags.HasPropertyExtensions)
            ) {
                PropertyTagExtensions = EPropertyTagExtension(reader.ReadByte().toByte())

                if (PropertyTagExtensions.HasFlag(EPropertyTagExtension.OverridableInformation)) {
                    OverrideOperation = EOverriddenPropertyOperation.entries[reader.ReadByte()]
                    bExperimentalOverridableLogic = reader.ReadBooleanInt()
                }
            }
        }
    }

    /** Writes a property to a BinaryWriter. */
    open fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext = PropertySerializationContext.Normal): Int {
        return 0
    }

    /** Initialize this property when serialized as zero. */
    internal open fun InitializeZero(reader: AssetBinaryReader) {
    }

    /** Complete writing the property tag of this property. */
    protected open fun WriteEndPropertyTag(writer: AssetBinaryWriter) {
        if (writer.Asset!!.HasUnversionedProperties) return

        if (writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME) {
            writer.WritePropertyGuid(PropertyGuid)
        }
        if (writer.Asset!!.ObjectVersionUE5 >= ObjectVersionUE5.PROPERTY_TAG_EXTENSION_AND_OVERRIDABLE_SERIALIZATION) {
            if (writer.Asset!!.ObjectVersionUE5 < ObjectVersionUE5.PROPERTY_TAG_COMPLETE_TYPE_NAME ||
                PropertyTagFlags.HasFlag(EPropertyTagFlags.HasPropertyExtensions)
            ) {
                writer.WriteByte(PropertyTagExtensions.value.toInt() and 0xFF)

                if (PropertyTagExtensions.HasFlag(EPropertyTagExtension.OverridableInformation)) {
                    writer.WriteByte(OverrideOperation.ordinal)
                    writer.WriteBooleanInt(bExperimentalOverridableLogic)
                }
            }
        }
    }

    /** Does the body of this property entirely consist of null bytes? */
    open fun CanBeZero(asset: UAsset): Boolean {
        val binaryWriter = AssetBinaryWriter(asset)
        this.Write(binaryWriter, false, PropertySerializationContext.CanBeZero)
        return binaryWriter.toByteArray().all { it == 0.toByte() }
    }

    /** Sets certain fields of the property based off of an array of strings. */
    open fun FromString(d: Array<String>, asset: UAsset) {
    }

    /** Performs a deep clone of the current PropertyData instance. */
    fun clone(): PropertyData {
        val res = CreateClone()
        // C# Clone() = MemberwiseClone() (shallow-copy every base + concrete field) + HandleCloned()
        // (per-type deep copies of the mutable fields). MemberwiseClone has no direct Kotlin
        // counterpart, so replicate it by reflection before running the per-type CloneInto.
        memberwiseCopy(this, res)
        CloneInto(res)
        HandleCloned(res)
        return res
    }

    private fun memberwiseCopy(from: PropertyData, to: PropertyData) {
        var cls: Class<*>? = from.javaClass
        while (cls != null && cls != Any::class.java) {
            for (field in cls.declaredFields) {
                if (java.lang.reflect.Modifier.isStatic(field.modifiers)) continue
                if (field.name.contains('$')) continue
                field.isAccessible = true
                field.set(to, field.get(from))
            }
            cls = cls.superclass
        }
    }

    /** Creates an empty instance of this property's concrete type (shallow). */
    protected open fun CreateClone(): PropertyData = throw NotImplementedError("Clone: ${this::class.simpleName}")

    /** Copies this instance's fields (base + concrete) into [res]. */
    protected open fun CloneInto(res: PropertyData) {
        res.Name = Name?.clone()
        res.Ancestry = Ancestry
        res.ArrayIndex = ArrayIndex
        res.PropertyGuid = PropertyGuid
        res.IsZero = IsZero
        res.PropertyTagFlags = PropertyTagFlags
        res.PropertyTypeName = PropertyTypeName
        res.PropertyTagExtensions = PropertyTagExtensions
        res.OverrideOperation = OverrideOperation
        res.bExperimentalOverridableLogic = bExperimentalOverridableLogic
        res.Offset = Offset
        res.Tag = Tag
        res.RawValue = cloneRawValue(RawValue)
    }

    protected open fun cloneRawValue(raw: Any?): Any? = when (raw) {
        is FName -> raw.clone()
        is FString -> raw.clone()
        else -> raw
    }

    protected open fun HandleCloned(res: PropertyData) {
    }

    companion object {
        private val FallbackPropertyType: FString = FString("")
    }
}

/** Kotlin replacement for C# `IStruct<T>` static interface members (see docs/mapping.md). */
class StructAccessors<T>(
    val read: (AssetBinaryReader) -> T,
    val fromString: (Array<String>, UAsset) -> T,
    val write: (AssetBinaryWriter, T) -> Int,
    val defaultValue: () -> T,
)

/** Kotlin replacement for C# `BasePropertyData<T> where T : IStruct<T>, new()`. */
abstract class BasePropertyData<T>(private val accessors: StructAccessors<T>) : PropertyData() {
    constructor(accessors: StructAccessors<T>, name: FName?) : this(accessors) {
        this.Name = name
    }

    open var Value: T?
        get() = GetObject()
        set(v) = SetObject(v)

    override val HasCustomStructSerialization: Boolean get() = true

    override fun Read(reader: AssetBinaryReader, includeHeader: Boolean, leng1: Long, leng2: Long, serializationContext: PropertySerializationContext) {
        if (includeHeader) {
            this.ReadEndPropertyTag(reader)
        }
        Value = accessors.read(reader)
    }

    override fun Write(writer: AssetBinaryWriter, includeHeader: Boolean, serializationContext: PropertySerializationContext): Int {
        if (includeHeader) {
            this.WriteEndPropertyTag(writer)
        }
        var v = Value
        if (v == null) {
            v = accessors.defaultValue()
            Value = v
        }
        return accessors.write(writer, v!!)
    }

    override fun FromString(d: Array<String>, asset: UAsset) {
        Value = accessors.fromString(d, asset)
    }
}
