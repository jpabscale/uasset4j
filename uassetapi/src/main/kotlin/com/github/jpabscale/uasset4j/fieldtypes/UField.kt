// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/FieldTypes/UField.cs
package com.github.jpabscale.uasset4j.fieldtypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.customversions.FFrameworkObjectVersion
import com.github.jpabscale.uasset4j.customversions.FReleaseObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.EPropertyFlags
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unversioned.UsmapPropertyType

open class UField() {
    var Next: FPackageIndex? = null

    open fun Read(reader: AssetBinaryReader) {
        if ((reader.Asset?.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFrameworkObjectVersion")) ?: -1) < FFrameworkObjectVersion.RemoveUField_Next.ordinal) {
            Next = FPackageIndex(reader.ReadInt32())
        }
    }

    open fun Write(writer: AssetBinaryWriter) {
        if ((writer.Asset?.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FFrameworkObjectVersion")) ?: -1) < FFrameworkObjectVersion.RemoveUField_Next.ordinal) {
            writer.WriteInt32(Next?.Index ?: 0)
        }
    }
}

abstract class UProperty() : UField() {
    var ArrayDim: EArrayDim = EArrayDim.NotAnArray
    var ElementSize: Int = 0
    var PropertyFlags: EPropertyFlags = EPropertyFlags(0L)
    var RepNotifyFunc: FName? = null
    var BlueprintReplicationCondition: ELifetimeCondition = ELifetimeCondition.COND_None

    var RawValue: Any? = null

    fun SetObject(value: Any?) {
        RawValue = value
    }

    fun <T> GetObject(): T {
        @Suppress("UNCHECKED_CAST")
        return RawValue as T
    }

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        ArrayDim = EArrayDim.fromValue(reader.ReadInt32())
        PropertyFlags = EPropertyFlags(reader.ReadUInt64())
        RepNotifyFunc = reader.ReadFName()

        if ((reader.Asset?.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FReleaseObjectVersion")) ?: -1) >= FReleaseObjectVersion.PropertiesSerializeRepCondition.ordinal) {
            BlueprintReplicationCondition = ELifetimeCondition.fromByte(reader.ReadByte())
        }
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(ArrayDim.ordinal)
        writer.WriteUInt64(PropertyFlags.value)
        writer.Write(RepNotifyFunc)

        if ((writer.Asset?.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FReleaseObjectVersion")) ?: -1) >= FReleaseObjectVersion.PropertiesSerializeRepCondition.ordinal) {
            writer.WriteByte(BlueprintReplicationCondition.value)
        }
    }

    fun GetUsmapPropertyType(): UsmapPropertyType {
        return when (this) {
            is UEnumProperty -> UsmapPropertyType.EnumProperty
            is UByteProperty -> UsmapPropertyType.ByteProperty
            is UBoolProperty -> UsmapPropertyType.BoolProperty
            is UInt8Property -> UsmapPropertyType.Int8Property
            is UInt16Property -> UsmapPropertyType.Int16Property
            is UIntProperty -> UsmapPropertyType.IntProperty
            is UInt64Property -> UsmapPropertyType.Int64Property
            is UUInt16Property -> UsmapPropertyType.UInt16Property
            is UUInt32Property -> UsmapPropertyType.UInt32Property
            is UUInt64Property -> UsmapPropertyType.UInt64Property
            is UFloatProperty -> UsmapPropertyType.FloatProperty
            is UDoubleProperty -> UsmapPropertyType.DoubleProperty

            is UAssetClassProperty -> UsmapPropertyType.SoftObjectProperty
            is USoftClassProperty -> UsmapPropertyType.SoftObjectProperty
            is UClassProperty -> UsmapPropertyType.ObjectProperty
            is UAssetObjectProperty -> UsmapPropertyType.AssetObjectProperty
            is UWeakObjectProperty -> UsmapPropertyType.WeakObjectProperty
            is ULazyObjectProperty -> UsmapPropertyType.LazyObjectProperty
            is USoftObjectProperty -> UsmapPropertyType.SoftObjectProperty
            is UObjectProperty -> UsmapPropertyType.ObjectProperty

            is UNameProperty -> UsmapPropertyType.NameProperty
            is UStrProperty -> UsmapPropertyType.StrProperty
            is UTextProperty -> UsmapPropertyType.TextProperty

            is UInterfaceProperty -> UsmapPropertyType.InterfaceProperty

            is UMulticastDelegateProperty -> UsmapPropertyType.MulticastDelegateProperty
            is UDelegateProperty -> UsmapPropertyType.DelegateProperty

            is UMapProperty -> UsmapPropertyType.MapProperty
            is USetProperty -> UsmapPropertyType.SetProperty
            is UArrayProperty -> UsmapPropertyType.ArrayProperty
            is UStructProperty -> UsmapPropertyType.StructProperty

            else -> UsmapPropertyType.Unknown
        }
    }
}

open class UEnumProperty() : UProperty() {
    var Enum: FPackageIndex = FPackageIndex()
    var UnderlyingProp: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)

        Enum = FPackageIndex(reader.ReadInt32())
        UnderlyingProp = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        writer.WriteInt32(Enum.Index)
        writer.WriteInt32(UnderlyingProp.Index)
    }
}

open class UArrayProperty() : UProperty() {
    var Inner: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        Inner = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(Inner.Index)
    }
}

open class USetProperty() : UProperty() {
    var ElementProp: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        ElementProp = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(ElementProp.Index)
    }
}

open class UObjectProperty() : UProperty() {
    var PropertyClass: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        PropertyClass = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(PropertyClass.Index)
    }
}

open class UWeakObjectProperty() : UObjectProperty()

open class USoftObjectProperty() : UObjectProperty()

open class ULazyObjectProperty() : UObjectProperty()

open class UAssetObjectProperty() : UObjectProperty()

open class UClassProperty() : UObjectProperty() {
    var MetaClass: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        MetaClass = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(MetaClass.Index)
    }
}

open class UAssetClassProperty() : UObjectProperty() {
    var MetaClass: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        MetaClass = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(MetaClass.Index)
    }
}

open class USoftClassProperty() : UObjectProperty() {
    var MetaClass: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        MetaClass = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(MetaClass.Index)
    }
}

open class UDelegateProperty() : UProperty() {
    var SignatureFunction: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        SignatureFunction = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(SignatureFunction.Index)
    }
}

open class UMulticastDelegateProperty() : UDelegateProperty()

open class UMulticastInlineDelegateProperty() : UMulticastDelegateProperty()

open class UMulticastSparseDelegateProperty() : UMulticastDelegateProperty()

open class UInterfaceProperty() : UProperty() {
    var InterfaceClass: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        InterfaceClass = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(InterfaceClass.Index)
    }
}

open class UMapProperty() : UProperty() {
    var KeyProp: FPackageIndex = FPackageIndex()
    var ValueProp: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        KeyProp = FPackageIndex(reader.ReadInt32())
        ValueProp = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(KeyProp.Index)
        writer.WriteInt32(ValueProp.Index)
    }
}

open class UBoolProperty() : UProperty() {
    var NativeBool: Boolean = false

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)

        ElementSize = reader.ReadByte()
        NativeBool = reader.ReadBooleanByte()
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteByte(ElementSize)
        writer.WriteBooleanByte(NativeBool)
    }
}

open class UByteProperty() : UProperty() {
    var Enum: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        Enum = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(Enum.Index)
    }
}

open class UStructProperty() : UProperty() {
    var Struct: FPackageIndex = FPackageIndex()

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        Struct = FPackageIndex(reader.ReadInt32())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(Struct.Index)
    }
}

open class UNameProperty() : UProperty()

open class UStrProperty() : UProperty()

open class UTextProperty() : UProperty()

open class UNumericProperty() : UProperty()

open class UDoubleProperty() : UNumericProperty()

open class UFloatProperty() : UNumericProperty()

open class UIntProperty() : UNumericProperty()

open class UInt8Property() : UNumericProperty()

open class UInt16Property() : UNumericProperty()

open class UInt64Property() : UNumericProperty()

open class UUInt16Property() : UNumericProperty()

open class UUInt32Property() : UNumericProperty()

open class UUInt64Property() : UNumericProperty()

open class UGenericProperty() : UProperty()
