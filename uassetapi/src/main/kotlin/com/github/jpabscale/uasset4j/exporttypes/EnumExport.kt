// Ported from UAssetAPI (MIT) — Copyright (c) 2020-2026 atenfyr
// Source: UAssetAPI/UAssetAPI/ExportTypes/EnumExport.cs
package com.github.jpabscale.uasset4j.exporttypes

import com.github.jpabscale.uasset4j.AssetBinaryReader
import com.github.jpabscale.uasset4j.AssetBinaryWriter
import com.github.jpabscale.uasset4j.CustomVersion
import com.github.jpabscale.uasset4j.UAsset
import com.github.jpabscale.uasset4j.customversions.FCoreObjectVersion
import com.github.jpabscale.uasset4j.unrealtypes.FName
import com.github.jpabscale.uasset4j.unrealtypes.ObjectVersion

/** How this enum is declared in C++. Affects the internal naming of enum values. */
enum class ECppForm {
    Regular,
    Namespaced,
    EnumClass;
}

/**
 * Reflection data for an enumeration.
 */
class UEnum {
    /** List of pairs of all enum names and values. */
    var Names: MutableList<Pair<FName, Long>> = mutableListOf()

    /** How the enum was originally defined. */
    var CppForm: ECppForm = ECppForm.Regular

    fun Read(reader: AssetBinaryReader, asset: UAsset) {
        if (asset.ObjectVersion < ObjectVersion.VER_UE4_TIGHTLY_PACKED_ENUMS) {
            val numEntries = reader.ReadInt32()
            for (i in 0 until numEntries) {
                val tempName = reader.ReadFName()
                Names.add(tempName to i.toLong())
            }
        } else if (asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FCoreObjectVersion")) < FCoreObjectVersion.EnumProperties.ordinal) {
            val numEntries = reader.ReadInt32()
            for (i in 0 until numEntries) {
                val tempName = reader.ReadFName()
                val tempVal = reader.ReadByte().toLong()
                Names.add(tempName to tempVal)
            }
        } else {
            val numEntries = reader.ReadInt32()
            for (i in 0 until numEntries) {
                val tempName = reader.ReadFName()
                val tempVal = reader.ReadInt64()
                Names.add(tempName to tempVal)
            }
        }

        if (asset.ObjectVersion < ObjectVersion.VER_UE4_ENUM_CLASS_SUPPORT) {
            val bIsNamespace = reader.ReadInt32() == 1
            CppForm = if (bIsNamespace) ECppForm.Namespaced else ECppForm.Regular
        } else {
            CppForm = ECppForm.entries[reader.ReadByte()]
        }
    }

    fun Write(writer: AssetBinaryWriter, asset: UAsset) {
        writer.WriteInt32(Names.size)
        if (asset.ObjectVersion < ObjectVersion.VER_UE4_TIGHTLY_PACKED_ENUMS) {
            val namesForSerialization = LinkedHashMap<Long, FName>()
            for (i in Names.indices) namesForSerialization[Names[i].second] = Names[i].first
            for (i in Names.indices) {
                if (namesForSerialization.containsKey(i.toLong())) writer.Write(namesForSerialization[i.toLong()])
            }
        } else if (asset.GetCustomVersion(CustomVersion.GetCustomVersionGuidFromFriendlyName("FCoreObjectVersion")) < FCoreObjectVersion.EnumProperties.ordinal) {
            for (i in Names.indices) {
                writer.Write(Names[i].first)
                writer.WriteByte(Names[i].second.toInt() and 0xFF)
            }
        } else {
            for (i in Names.indices) {
                writer.Write(Names[i].first)
                writer.WriteInt64(Names[i].second)
            }
        }

        if (asset.ObjectVersion < ObjectVersion.VER_UE4_ENUM_CLASS_SUPPORT) {
            writer.WriteBooleanInt(CppForm == ECppForm.Namespaced)
        } else {
            writer.WriteByte(CppForm.ordinal)
        }
    }

    constructor() {
        Names = mutableListOf()
    }
}

/**
 * Export data for an enumeration. See [UEnum].
 */
class EnumExport : NormalExport {
    /** The enum that is stored in this export. */
    var Enum: UEnum? = null

    constructor(superExport: Export) : super(superExport)

    constructor(asset: UAsset?, extras: ByteArray?) : super(asset, extras)

    constructor() : super()

    override fun Read(reader: AssetBinaryReader, nextStarting: Int) {
        super.Read(reader, nextStarting)

        Enum = UEnum()
        Enum!!.Read(reader, Asset!!)
    }

    override fun Write(writer: AssetBinaryWriter) {
        super.Write(writer)

        Enum!!.Write(writer, Asset!!)
    }
}
