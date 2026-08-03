// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/FieldTypes/FField.cs
package com.github.jpabscale.uasset4j.fieldtypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.EPackageFlags
import com.github.jpabscale.uasset4j.MainSerializer
import com.github.jpabscale.uasset4j.unrealtypes.EObjectFlags
import com.github.jpabscale.uasset4j.unrealtypes.EPropertyFlags
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.FString
import com.github.jpabscale.uasset4j.unversioned.UsmapPropertyType

open class FField() {
    var SerializedType: FName? = null
    var Name: FName? = null
    var Flags: EObjectFlags = EObjectFlags(0L)
    var MetaDataMap: LinkedHashMap<FName, FString?>? = null

    open fun Read(reader: AssetBinaryReader) {
        Name = reader.ReadFName()
        Flags = EObjectFlags(reader.ReadUInt32())

        if (!reader.Asset!!.IsFilterEditorOnly && !reader.Asset!!.PackageFlags.HasFlag(EPackageFlags.PKG_Cooked)) {
            val bHasMetaData = reader.ReadBooleanInt()
            if (bHasMetaData) {
                MetaDataMap = reader.ReadMap({ reader.ReadFName() }, { reader.ReadFString() })
            }
        }
    }

    open fun Write(writer: AssetBinaryWriter) {
        writer.Write(Name)
        writer.WriteUInt32(Flags.value)

        if (!writer.Asset!!.IsFilterEditorOnly && !writer.Asset!!.PackageFlags.HasFlag(EPackageFlags.PKG_Cooked)) {
            writer.WriteBooleanInt(MetaDataMap != null)
            if (MetaDataMap != null && MetaDataMap!!.size > 0) {
                writer.WriteInt32(MetaDataMap!!.size)
                for ((key, value) in MetaDataMap!!) {
                    writer.Write(key)
                    writer.Write(value)
                }
            }
        }
    }
}

abstract class FProperty() : FField() {
    var ArrayDim: EArrayDim = EArrayDim.NotAnArray
    var ElementSize: Int = 0
    var PropertyFlags: EPropertyFlags = EPropertyFlags(0L)
    var RepIndex: Int = 0
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

    private val UsmapPropertyTypeOverrides: LinkedHashMap<String, UsmapPropertyType> = linkedMapOf(
        "MulticastInlineDelegateProperty" to UsmapPropertyType.MulticastDelegateProperty,
        "ClassProperty" to UsmapPropertyType.ObjectProperty,
        "SoftClassProperty" to UsmapPropertyType.SoftObjectProperty
    )

    fun GetUsmapPropertyType(): UsmapPropertyType {
        val serializedType = SerializedType?.Value?.Value
        val override = UsmapPropertyTypeOverrides[serializedType]
        if (override != null) return override
        return UsmapPropertyType.entries.firstOrNull { it.name.equals(serializedType, true) } ?: UsmapPropertyType.Unknown
    }

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        ArrayDim = EArrayDim.fromValue(reader.ReadInt32())
        ElementSize = reader.ReadInt32()
        PropertyFlags = EPropertyFlags(reader.ReadUInt64())
        RepIndex = reader.ReadUInt16()
        RepNotifyFunc = reader.ReadFName()
        BlueprintReplicationCondition = ELifetimeCondition.fromByte(reader.ReadByte())
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteInt32(ArrayDim.ordinal)
        writer.WriteInt32(ElementSize)
        writer.WriteUInt64(PropertyFlags.value)
        writer.WriteUInt16(RepIndex)
        writer.Write(RepNotifyFunc)
        writer.WriteByte(BlueprintReplicationCondition.value)
    }
}

open class FEnumProperty() : FProperty() {
    var Enum: FPackageIndex = FPackageIndex()
    var UnderlyingProp: FProperty? = null

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)

        Enum = FPackageIndex(reader.ReadInt32())
        UnderlyingProp = MainSerializer.ReadFProperty(reader)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        writer.WriteInt32(Enum.Index)
        MainSerializer.WriteFProperty(UnderlyingProp!!, writer)
    }
}

open class FArrayProperty() : FProperty() {
    var Inner: FProperty? = null

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        Inner = MainSerializer.ReadFProperty(reader)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        MainSerializer.WriteFProperty(Inner!!, writer)
    }
}

open class FSetProperty() : FProperty() {
    var ElementProp: FProperty? = null

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        ElementProp = MainSerializer.ReadFProperty(reader)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        MainSerializer.WriteFProperty(ElementProp!!, writer)
    }
}

open class FObjectProperty() : FProperty() {
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

open class FSoftObjectProperty() : FObjectProperty()

open class FWeakObjectProperty() : FObjectProperty()

open class FClassProperty() : FObjectProperty() {
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

open class FSoftClassProperty() : FObjectProperty() {
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

open class FDelegateProperty() : FProperty() {
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

open class FMulticastDelegateProperty() : FDelegateProperty()

open class FMulticastInlineDelegateProperty() : FMulticastDelegateProperty()

open class FInterfaceProperty() : FProperty() {
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

open class FMapProperty() : FProperty() {
    var KeyProp: FProperty? = null
    var ValueProp: FProperty? = null

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        KeyProp = MainSerializer.ReadFProperty(reader)
        ValueProp = MainSerializer.ReadFProperty(reader)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        MainSerializer.WriteFProperty(KeyProp!!, writer)
        MainSerializer.WriteFProperty(ValueProp!!, writer)
    }
}

open class FBoolProperty() : FProperty() {
    var FieldSize: Byte = 0
    var ByteOffset: Byte = 0
    var ByteMask: Byte = 0
    var FieldMask: Byte = 0

    var NativeBool: Boolean = false
    var Value: Boolean = false

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)

        FieldSize = reader.ReadByte().toByte()
        ByteOffset = reader.ReadByte().toByte()
        ByteMask = reader.ReadByte().toByte()
        FieldMask = reader.ReadByte().toByte()
        NativeBool = reader.ReadBooleanByte()
        Value = reader.ReadBooleanByte()
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        writer.WriteByte(FieldSize.toInt())
        writer.WriteByte(ByteOffset.toInt())
        writer.WriteByte(ByteMask.toInt())
        writer.WriteByte(FieldMask.toInt())
        writer.WriteBooleanByte(NativeBool)
        writer.WriteBooleanByte(Value)
    }
}

open class FByteProperty() : FProperty() {
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

open class FStructProperty() : FProperty() {
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

open class FNumericProperty() : FProperty() {
    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
    }
}

open class FGenericProperty() : FProperty() {
    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
    }
}

open class FOptionalProperty() : FProperty() {
    var ValueProperty: FProperty? = null

    override fun Read(reader: AssetBinaryReader) {
        super.Read(reader)
        ValueProperty = MainSerializer.ReadFProperty(reader)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)
        MainSerializer.WriteFProperty(ValueProperty!!, writer)
    }
}
