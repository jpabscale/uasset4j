// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/ClassExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.UnrealBinaryWriter
import com.github.jpabscale.uasset4j.unrealtypes.EClassFlags
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.FPackageIndex
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion

/**
 * Represents an interface that a UClass ([ClassExport]) implements.
 */
class SerializedInterfaceReference {
    var Class: Int = 0
    var PointerOffset: Int = 0
    var bImplementedByK2: Boolean = false

    constructor(@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") class_: Int, pointerOffset: Int, bImplementedByK2: Boolean) {
        Class = class_
        PointerOffset = pointerOffset
        this.bImplementedByK2 = bImplementedByK2
    }

    constructor()
}

/**
 * Represents an object class.
 */
class ClassExport : StructExport {
    /** Map of all functions by name contained in this class. */
    var FuncMap: LinkedHashMap<FName, FPackageIndex>? = null

    /** Class flags; See [EClassFlags] for more information. */
    var ClassFlags: EClassFlags = EClassFlags(EClassFlags.CLASS_None)

    /** The required type for the outer of instances of this class. */
    var ClassWithin: FPackageIndex? = null

    /** Which Name.ini file to load Config variables out of. */
    var ClassConfigName: FName? = null

    /**
     * The list of interfaces which this class implements, along with the pointer property that is located at the offset of the interface's vtable.
     * If the interface class isn't native, the property will be empty.
     */
    var Interfaces: Array<SerializedInterfaceReference>? = null

    /** This is the blueprint that caused the generation of this class, or null if it is a native compiled-in class. */
    var ClassGeneratedBy: FPackageIndex? = null

    /** Does this class use deprecated script order? */
    var bDeprecatedForceScriptOrder: Boolean = false

    /** Used to check if the class was cooked or not. */
    var bCooked: Boolean = false

    /** The class default object; used for delta serialization and object initialization. */
    var ClassDefaultObject: FPackageIndex? = null

    constructor(superExport: Export) : super(superExport)

    constructor(asset: UAsset?, extras: ByteArray?) : super(asset, extras)

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)

        val numFuncIndexEntries = reader.ReadInt32()
        FuncMap = LinkedHashMap()
        for (i in 0 until numFuncIndexEntries) {
            val functionName = reader.ReadFName()
            val functionExport = FPackageIndex.FromRawIndex(reader.ReadInt32())

            FuncMap!![functionName] = functionExport
        }

        ClassFlags = EClassFlags(reader.ReadUInt32().toInt())

        if (Asset!!.ObjectVersion < ObjectVersion.VER_UE4_CLASS_NOTPLACEABLE_ADDED) {
            ClassFlags = EClassFlags(ClassFlags.value xor EClassFlags.CLASS_NotPlaceable)
        }

        ClassWithin = FPackageIndex(reader.ReadInt32())
        ClassConfigName = reader.ReadFName()
        Asset!!.AddNameReference(ClassConfigName!!.Value!!)

        var numInterfaces = 0
        var interfacesStart = 0L
        if (Asset!!.ObjectVersion < ObjectVersion.VER_UE4_UCLASS_SERIALIZE_INTERFACES_AFTER_LINKING) {
            interfacesStart = reader.position.toLong()
            numInterfaces = reader.ReadInt32()
            reader.position = (interfacesStart + 4 + numInterfaces * 12).toInt()
        }

        // Linking procedure here; I don't think anything is really serialized during this
        ClassGeneratedBy = FPackageIndex(reader.ReadInt32())

        val currentOffset = reader.position
        if (Asset!!.ObjectVersion < ObjectVersion.VER_UE4_UCLASS_SERIALIZE_INTERFACES_AFTER_LINKING) {
            reader.position = interfacesStart.toInt()
        }
        numInterfaces = reader.ReadInt32()
        Interfaces = Array(numInterfaces) {
            SerializedInterfaceReference(reader.ReadInt32(), reader.ReadInt32(), reader.ReadInt32() == 1)
        }
        if (Asset!!.ObjectVersion < ObjectVersion.VER_UE4_UCLASS_SERIALIZE_INTERFACES_AFTER_LINKING) {
            reader.position = currentOffset
        }

        bDeprecatedForceScriptOrder = reader.ReadInt32() == 1

        reader.ReadInt64() // None

        if (Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_ADD_COOKED_TO_UCLASS) {
            bCooked = reader.ReadInt32() == 1
        }

        ClassDefaultObject = FPackageIndex(reader.ReadInt32())

        // CDO serialization usually comes after this export has finished serializing
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        writer.WriteInt32(FuncMap!!.size)
        for (i in 0 until FuncMap!!.size) {
            writer.Write(FuncMap!!.keys.elementAt(i))
            writer.WriteInt32(FuncMap!!.values.elementAt(i).Index)
        }

        var serializingClassFlags = ClassFlags
        if (Asset!!.ObjectVersion < ObjectVersion.VER_UE4_CLASS_NOTPLACEABLE_ADDED) {
            serializingClassFlags = EClassFlags(serializingClassFlags.value xor EClassFlags.CLASS_NotPlaceable)
        }
        writer.WriteUInt32(serializingClassFlags.value.toLong() and 0xFFFFFFFFL)

        writer.WriteInt32(ClassWithin?.Index ?: 0)
        writer.Write(ClassConfigName)

        if (Asset!!.ObjectVersion < ObjectVersion.VER_UE4_UCLASS_SERIALIZE_INTERFACES_AFTER_LINKING) {
            SerializeInterfaces(writer)
        }

        // Linking procedure here; I don't think anything is really serialized during this
        writer.WriteInt32(ClassGeneratedBy?.Index ?: 0)

        if (Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_UCLASS_SERIALIZE_INTERFACES_AFTER_LINKING) {
            SerializeInterfaces(writer)
        }

        writer.WriteBooleanInt(bDeprecatedForceScriptOrder)

        writer.Write(FName(writer.Asset, "None"))

        if (Asset!!.ObjectVersion >= ObjectVersion.VER_UE4_ADD_COOKED_TO_UCLASS) {
            writer.WriteBooleanInt(bCooked)
        }

        writer.WriteInt32(ClassDefaultObject?.Index ?: 0)
    }

    private fun SerializeInterfaces(writer: UnrealBinaryWriter) {
        writer.WriteInt32(Interfaces!!.size)
        for (i in Interfaces!!.indices) {
            writer.WriteInt32(Interfaces!![i].Class)
            writer.WriteInt32(Interfaces!![i].PointerOffset)
            writer.WriteBooleanInt(Interfaces!![i].bImplementedByK2)
        }
    }
}
